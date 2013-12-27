package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.mario.utils.DynamicLagReducer;
import net.stormdev.mario.utils.MarioKartSound;
import net.stormdev.mario.utils.PlayerQuitException;
import net.stormdev.mario.utils.RaceQueue;
import net.stormdev.mario.utils.RaceTrack;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.useful.ucarsCommon.StatValue;

public class RaceScheduler {
	private ConcurrentHashMap<UUID, Race> races = new ConcurrentHashMap<UUID, Race>();
	private int raceLimit = 5;

	public RaceScheduler(int raceLimit) {
		this.raceLimit = raceLimit;
	}

	public synchronized void joinAutoQueue(Player player, RaceType type) {
		Map<UUID, RaceQueue> queues = main.plugin.raceQueues
				.getOpenQueues(type); // Joinable queues for that racemode
		RaceQueue toJoin = null;
		Boolean added = false;
		List<RaceTrack> tracks = main.plugin.trackManager.getRaceTracks();
		List<RaceTrack> openTracks = new ArrayList<RaceTrack>();
		List<RaceTrack> openNoQueueTracks = new ArrayList<RaceTrack>();
		for (RaceTrack t : tracks) {
			if (!isTrackInUse(t, type)) {
				openTracks.add(t);
				if (!main.plugin.raceQueues.queuesFor(t, type)) {
					openNoQueueTracks.add(t);
				}
				if (main.plugin.raceQueues.getQueues(t.getTrackName())
						.size() < 1) {
					openNoQueueTracks.add(t);
				}
			}
		}
		if (queues.size() > 0 && type != RaceType.TIME_TRIAL) { // Check if
																// queues
																// existing and
																// for Time
																// Trial force a
																// new queue
			int targetPlayers = main.config
					.getInt("general.race.targetPlayers");
			Map<UUID, RaceQueue> recommendedQueues = new HashMap<UUID, RaceQueue>();
			for (UUID id : new ArrayList<UUID>(queues.keySet())) {
				RaceQueue queue = queues.get(id);
				if (queue.playerCount() < targetPlayers) {
					recommendedQueues.put(id, queue);
				}
			}
			if (recommendedQueues.size() > 0) {
				UUID random = (UUID) recommendedQueues.keySet().toArray()[main.plugin.random
						.nextInt(recommendedQueues.size())];
				toJoin = recommendedQueues.get(random);
			} else {
				if(main.plugin.random.nextBoolean() && openNoQueueTracks.size() > 0){
					//Chance that will join a new track in a new queue
					RaceTrack t = openNoQueueTracks.get(main.plugin.random.nextInt(
							openNoQueueTracks.size()));
					toJoin = new RaceQueue(t, type, player);
				}
				else{
					// Join from 'queues'
					UUID random = (UUID) queues.keySet().toArray()[main.plugin.random
					                       						.nextInt(queues.size())];
					                       				toJoin = queues.get(random);
				}
			}
		} else {
			// Create a random queue
			RaceTrack track = null;
			if (openNoQueueTracks.size() > 0 && type != RaceType.TIME_TRIAL) {
				track = openNoQueueTracks.get(main.plugin.random
						.nextInt(openNoQueueTracks.size()));
			} else {
				if (openNoQueueTracks.size() > 0) {
					track = openNoQueueTracks.get(main.plugin.random
							.nextInt(openNoQueueTracks.size()));
				} else {
					if (type == RaceType.TIME_TRIAL
							&& openNoQueueTracks.size() > 0) {
						// Put them on a track to themselves
						track = openNoQueueTracks.get(main.plugin.random
								.nextInt(openNoQueueTracks.size()));
					} else {
						if (tracks.size() < 1) {
							// No tracks exist
							// No tracks created
							player.sendMessage(main.colors.getError()
									+ main.msgs.get("general.cmd.delete.exists"));
							return;
						}
						//All queues and tracks full...
						player.sendMessage(main.colors.getError()
								+ main.msgs.get("general.cmd.overflow"));
						track = tracks.get(main.plugin.random.nextInt(tracks.size()));
						//Joining a new queue for that track (Low priority)
					}
				}
			}
			if (track == null) {
				//Track doesn't exist
				player.sendMessage(main.colors.getError()
						+ main.msgs.get("general.cmd.delete.exists"));
				return;
			}
			toJoin = new RaceQueue(track, type, player);
			added = true;
		}
		// Join that queue
		if (!added) {
			toJoin.addPlayer(player);
		}
		toJoin.broadcast(main.colors.getTitle() + "[MarioKart:] "
				+ main.colors.getInfo() + player.getName()
				+ main.msgs.get("race.que.joined") + " ["
				+ toJoin.playerCount() + "/" + toJoin.playerLimit() + "]");
		executeLobbyJoin(player, toJoin);
		recalculateQueues();
		return;
	}

	public synchronized void joinQueue(Player player, RaceTrack track, RaceType type) {
		Map<UUID, RaceQueue> queues = main.plugin.raceQueues.getQueues(track.getTrackName(), type); // Get the oldest queue of that type for that track
		RaceQueue queue = null;
		if (queues.size() < 1) {
			queue = new RaceQueue(track, type, player);
		} else {
			for(UUID id:queues.keySet()){
				RaceQueue q = queues.get(id);
				if(q.playerCount() < q.playerLimit()){
					queue = q;
				}
			}
			if(queue == null){ //No queues of that type available, so create and schedule a new one
				queue = new RaceQueue(track, type, player);
				player.sendMessage(main.colors.getInfo()
						+ main.msgs.get("general.cmd.overflow"));
			}
		}
		queue.broadcast(main.colors.getTitle() + "[MarioKart:] "
				+ main.colors.getInfo() + player.getName()
				+ main.msgs.get("race.que.joined") + " [" + queue.playerCount()
				+ "/" + queue.playerLimit() + "]");
		executeLobbyJoin(player, queue);
		recalculateQueues();
		return;
	}

	public void executeLobbyJoin(Player player, RaceQueue queue) {
		Location l = queue.getTrack().getLobby(main.plugin.getServer());
		Chunk chunk = l.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
		}
		player.teleport(l);
		String rl = main.plugin.packUrl;
		player.sendMessage(main.colors.getInfo()
				+ main.msgs.get("resource.download"));
		String msg = main.msgs.get("resource.downloadHelp");
		msg = msg.replaceAll(Pattern.quote("%url%"),
				Matcher.quoteReplacement(ChatColor.RESET + rl));
		player.sendMessage(main.colors.getInfo() + msg);
		//TODO Use setResourePack, but would remove old version support
		if(!main.plugin.resourcedPlayers.contains(player.getName())){
			player.setTexturePack(main.config.getString("mariokart.resourcePack"));
			main.plugin.resourcedPlayers.add(player.getName());
		}
		return;
	}

	public void leaveQueue(Player player, RaceQueue queue) {
		queue.removePlayer(player);
		return;
	}

	public synchronized void recalculateQueues() {
		if (getRacesRunning() >= raceLimit) {
			main.logger.info("[INFO] Max races running");
			return; // Cannot start any more races for now...
		}
		/* Don't bother
		if(DynamicLagReducer.getResourceScore() < 30){
			main.logger.info("Delayed re-queueing due to lack of server resources!");
			if(getRacesRunning() < 1){ //Else when race ends it will fire again anyway
				main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){
					@Override
					public void run() {
						//Make sure queues don't lock
						recalculateQueues();
						return;
					}}, 600l);
			}
			return; //Not enough memory/resource to run more races
		}
		*/
		Map<UUID, RaceQueue> queues = main.plugin.raceQueues.getAllQueues();
		ArrayList<RaceTrack> queuedTracks = new ArrayList<RaceTrack>();
		for (UUID id : new ArrayList<UUID>(queues.keySet())) {
			final RaceQueue queue = queues.get(id);
			if (queue.getRaceMode() == RaceType.TIME_TRIAL
					&& !isTrackInUse(queue.getTrack(), RaceType.TIME_TRIAL)
					&& !queuedTracks.contains(queue.getTrack()) // Are there
																// other
																// racemodes
																// waiting for
																// the track
																// ahead of it?
					&& getRacesRunning() < raceLimit && !queue.isStarting()) {
				//Time trial races
				double predicted = 110; //Predicted Memory needed
				if(DynamicLagReducer.getResourceScore(predicted) < 30){
					main.logger.info("Delayed re-queueing due to lack of server resources!");
					if(getRacesRunning() < 1){
						main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){
						@Override
						public void run() {
							//Make sure queues don't lock
							recalculateQueues();
							return;
						}}, 600l);
					}
					return; //Cancel - Not enough memory
				}
				//Memory should be available
				queue.setStarting(true);
				List<Player> q = new ArrayList<Player>(queue.getPlayers());
				for (Player p : q) {
					if (p != null && p.isOnline()
							&& getRacesRunning() < raceLimit) {
						Race race = new Race(queue.getTrack(),
								queue.getTrackName(), RaceType.TIME_TRIAL);
						race.join(p);
						if (race.howManyPlayers() > 0) {
							startRace(race.getTrackName(), race);
						}
						queue.removePlayer(p);
					}
				}
				if (queue.playerCount() < 1) {
					q.clear();
					main.plugin.raceQueues.removeQueue(queue);
				}
			} else if (queue.playerCount() >= main.config
					.getInt("race.que.minPlayers")
					&& !isTrackInUse(queue.getTrack(), queue.getRaceMode())
					&& getRacesRunning() < raceLimit
					&& !queuedTracks.contains(queue.getTrack()) // Check it's
																// not reserved
					&& queue.getRaceMode() != RaceType.TIME_TRIAL
					&& !queue.isStarting()) {
				int c = queue.playerCount();
				double predicted = c*60+50; //Predicted Memory needed
				if(DynamicLagReducer.getResourceScore(predicted) < 30){
					main.logger.info("Delayed re-queueing due to lack of server resources!");
					if(getRacesRunning() < 1){
						main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){
						@Override
						public void run() {
							//Make sure queues don't lock
							recalculateQueues();
							return;
						}}, 600l);
					}
					return; //Cancel - Not enough memory
				}
				queuedTracks.add(queue.getTrack());
				// Queue can be initiated
				queue.setStarting(true);
				// Wait grace time
				double graceS = main.config
						.getDouble("general.raceGracePeriod");
				long grace = (long) (graceS * 20);
				String msg = main.msgs.get("race.que.players");
				msg = msg.replaceAll(Pattern.quote("%time%"), "" + graceS);
				queue.broadcast(main.colors.getInfo() + msg);
				main.plugin.getServer().getScheduler()
						.runTaskLater(main.plugin, new Runnable() {

							@Override
							public void run() {
								if (queue.playerCount() < main.config
										.getInt("race.que.minPlayers")) {
									queue.setStarting(false);
									return;
								}
								Race race = new Race(queue.getTrack(), queue
										.getTrackName(), queue.getRaceMode());
								List<Player> q = new ArrayList<Player>(queue
										.getPlayers());
								for (Player p : q) {
									if (p != null && p.isOnline()) {
										race.join(p);
									}
								}
								q.clear();
								if (race.howManyPlayers() >= main.config
										.getInt("race.que.minPlayers")) {
									queue.clear();
									main.plugin.raceQueues.removeQueue(queue);
									startRace(race.getTrackName(), race);
								} else {
									queue.setStarting(false);
								}
								return;
							}
						}, grace);
			} else {
				// Race unable to be started (Unavailable etc...)
				if (queue.getRaceMode() != RaceType.TIME_TRIAL) {
					queuedTracks.add(queue.getTrack());
				}
			}
			if (getRacesRunning() >= raceLimit) {
				main.logger.info("[INFO] Max races running");
				return; // No more races can be run for now
			}
		}
	}

	public synchronized void startRace(String trackName, final Race race) {
		this.races.put(race.getGameId(), race);
		final List<User> users = new ArrayList<User>(race.getUsers());
		for (User user : users) {
			Player player = null;
			try {
				player = user.getPlayer();
			} catch (PlayerQuitException e) {
				race.leave(user, true);
				// User has left
			}
			user.setOldInventory(player.getInventory().getContents().clone());
			if (player != null) {
				player.getInventory().clear();
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
		final ArrayList<Minecart> cars = new ArrayList<Minecart>();
		RaceTrack track = race.getTrack();
		ArrayList<SerializableLocation> sgrid = track.getStartGrid();
		HashMap<Integer, Location> grid = new HashMap<Integer, Location>();
		for (int i = 0; i < sgrid.size(); i++) {
			SerializableLocation s = sgrid.get(i);
			grid.put(i, s.getLocation(main.plugin.getServer()).clone());
		}
		int count = grid.size();
		if (count > users.size()) { // If more grid slots than players, only
			// use the right number of grid slots
			count = users.size();
		}
		if (users.size() > count) {
			count = users.size(); // Should theoretically never happen but
			// sometimes does?
		}
		for (int i = 0; i < count; i++) {
			int max = users.size();
			if (max>0) {
				Player p = null;
				int randomNumber = main.plugin.random.nextInt(max);
				User user = users.get(randomNumber);
				try {
					p = users.get(randomNumber).getPlayer();
				} catch (PlayerQuitException e) {
					// Player has left
				}
				users.remove(user);
				Location loc = grid.get(i);
				if (race.getType() == RaceType.TIME_TRIAL) {
					loc = grid.get(main.plugin.random.nextInt(grid.size()));
				}
				if (p != null) {
					if (p.getVehicle() != null) {
						p.getVehicle().eject();
					}
					Chunk c = loc.getChunk();
					if (c.isLoaded()) {
						c.load(true);
					}
					p.teleport(loc.add(0, 2, 0));
					Minecart car = (Minecart) loc.getWorld().spawnEntity(
							loc.add(0, 0.2, 0), EntityType.MINECART);
					car.setMetadata("car.frozen", new StatValue(null,
							main.plugin));
					car.setMetadata("kart.racing", new StatValue(null,
							main.plugin));
					car.setPassenger(p);
					p.setMetadata("car.stayIn",
							new StatValue(null, main.plugin));
					cars.add(car);
				}
			}
		}
		if (users.size() > 0) {
			User user = users.get(0);
			try {
				Player p = user.getPlayer();
				p.sendMessage(main.colors.getError()
						+ main.msgs.get("race.que.full"));
			} catch (PlayerQuitException e) {
				// Player has left anyway
			}
			race.leave(user, true);
		}

		for (User user : users) {
			Player player;
			try {
				player = user.getPlayer();
				user.setLocation(player.getLocation().clone());
				player.sendMessage(main.colors.getInfo()
						+ main.msgs.get("race.que.preparing"));
			} catch (PlayerQuitException e) {
				// Player has left
			}
		}
		final List<User> users2 = new ArrayList<User>(race.getUsers());
		for (User user2 : users2) {
			user2.setInRace(true);
		}
		main.plugin.getServer().getScheduler()
				.runTaskAsynchronously(main.plugin, new Runnable() {
					public void run() {
						for (User user : users2) {
							try {
								user.getPlayer()
										.sendMessage(
												main.colors.getInfo()
														+ main.msgs
																.get("race.que.starting"));
							} catch (PlayerQuitException e) {
								// User has left
							}
						}
						for (int i = 10; i > 0; i--) {
							try {
								if (i == 10) {
									//Beginning of race countdown sound...
									try {
										for(User u:users2){
											try {
												Player p = u.getPlayer();
												if(p!=null){
													main.plugin.playCustomSound(p, MarioKartSound.RACE_START_COUNTDOWN);
												}
											} catch (Exception e) {
												//Player has left
											}
										}
									} catch (Exception e) {
										// Player has left
									}
								}
								if (i == 3) {
									//Last 3..2..1.. countdown sound
									try {
										for(User u:users2){
											try {
												Player p = u.getPlayer();
												if(p!=null){
													main.plugin.playCustomSound(p, MarioKartSound.COUNTDOWN_PLING);
												}
											} catch (Exception e) {
												//Player has left
											}
										}
									} catch (Exception e) {
										// Player has left
									}
								}
							} catch (Exception e) {
								// Game ended
							}
							for (User user : users2) {
								try {
									Player p = user.getPlayer();
									p.sendMessage(main.colors.getInfo() + ""
											+ i);
								} catch (PlayerQuitException e) {
									// Player has left
								}
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						}
						for (Minecart car : cars) {
							car.removeMetadata("car.frozen", main.plugin);
						}
						for (User user : users2) {
							try {
								user.getPlayer().sendMessage(
										main.colors.getInfo()
												+ main.msgs.get("race.que.go"));
							} catch (PlayerQuitException e) {
								// Player has left
							}
						}
						race.start();
						return;
					}
				});

		return;
	}

	public synchronized void stopRace(Race race) {
		race.end();
		race.clear();
		this.races.put(race.getGameId(), race);
		removeRace(race);
		recalculateQueues();
	}

	public synchronized void removeRace(Race race) {
		race.clear();
		this.races.remove(race.getGameId());
	}

	public void updateRace(Race race) {
		if(race == null || race.getGameId() == null){
			return;
		}
		if(this.races == null){
			this.races = new ConcurrentHashMap<UUID, Race>();
		}
		if (this.races.containsKey(race.getGameId())) {
			this.races.put(race.getGameId(), race);
		}
	}

	public ConcurrentHashMap<UUID, Race> getRaces() {
		return races;
	}

	public int getRacesRunning() {
		return races.size();
	}

	public Boolean isTrackInUse(RaceTrack track, RaceType type) {
		for (UUID id : races.keySet()) {
			Race r = races.get(id);
			if (r.getTrackName().equals(track.getTrackName())) {
				if (type == RaceType.TIME_TRIAL
						&& r.getType() == RaceType.TIME_TRIAL) {
					return false;
				}
				return true;
			}
		}
		return false;
	}

}
