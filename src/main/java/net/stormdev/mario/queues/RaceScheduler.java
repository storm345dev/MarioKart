package net.stormdev.mario.queues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.mario.lesslag.DynamicLagReducer;
import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.PlayerQuitException;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.sound.MarioKartSound;
import net.stormdev.mario.tracks.RaceTrack;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucarsCommon.StatValue;

public class RaceScheduler {
	private HashMap<UUID, Race> races = new HashMap<UUID, Race>();
	private int raceLimit = 5;
	private boolean lockdown = false;
	private boolean fairCars = true;

	public RaceScheduler(int raceLimit) {
		if(MarioKart.fullServer){
			raceLimit = 1;
		}
		this.raceLimit = raceLimit;
		fairCars = MarioKart.config.getBoolean("general.ensureEqualCarSpeed");
	}

	public void joinAutoQueue(Player player, RaceType type) {
		if(lockdown){
			player.sendMessage(MarioKart.colors.getError()+MarioKart.msgs.get("error.memoryLockdown"));
			return;
		}
		Map<UUID, RaceQueue> queues = MarioKart.plugin.raceQueues
				.getOpenQueues(type); // Joinable queues for that racemode
		RaceQueue toJoin = null;
		Boolean added = false;
		List<RaceTrack> tracks = MarioKart.plugin.trackManager.getRaceTracks();
		List<RaceTrack> openTracks = new ArrayList<RaceTrack>();
		List<RaceTrack> openNoQueueTracks = new ArrayList<RaceTrack>();
		List<RaceTrack> NoQueueTracks = new ArrayList<RaceTrack>();
		for (RaceTrack t : tracks) {
			if (!isTrackInUse(t, type)) {
				openTracks.add(t);
				if (!MarioKart.plugin.raceQueues.queuesFor(t, type)) {
					openNoQueueTracks.add(t);
				}
			}
			if (!MarioKart.plugin.raceQueues.queuesFor(t, type)) {
				NoQueueTracks.add(t);
			}
		}
		if (queues.size() > 0) {
			int targetPlayers = MarioKart.config
					.getInt("general.race.targetPlayers");
			Map<UUID, RaceQueue> recommendedQueues = new HashMap<UUID, RaceQueue>();
			for (UUID id : new ArrayList<UUID>(queues.keySet())) {
				RaceQueue queue = queues.get(id);
				if (queue.playerCount() < targetPlayers) {
					recommendedQueues.put(id, queue);
				}
			}
			if (recommendedQueues.size() > 0) {
				UUID random = (UUID) recommendedQueues.keySet().toArray()[MarioKart.plugin.random
						.nextInt(recommendedQueues.size())];
				toJoin = recommendedQueues.get(random);
			} else {
				if(MarioKart.plugin.random.nextBoolean() && openNoQueueTracks.size() > 0){
					//Chance that will join a new track in a new queue
					RaceTrack t = openNoQueueTracks.get(MarioKart.plugin.random.nextInt(
							openNoQueueTracks.size()));
					toJoin = new RaceQueue(t, type, player);
				}
				else{
					// Join from 'queues'
					UUID random = (UUID) queues.keySet().toArray()[MarioKart.plugin.random
					                       						.nextInt(queues.size())];
					                       				toJoin = queues.get(random);
				}
			}
		} else {
			// Create a random queue
			RaceTrack track = null;
			if (openNoQueueTracks.size() > 0) {
				track = openNoQueueTracks.get(MarioKart.plugin.random
						.nextInt(openNoQueueTracks.size()));
			} else {
				if (tracks.size() < 1) {
					// No tracks exist
					// No tracks created
					player.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.delete.exists"));
					return;
				}
				//All queues and tracks full...
				player.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("general.cmd.overflow"));
				track = tracks.get(MarioKart.plugin.random.nextInt(tracks.size()));
				//Joining a new queue for that track (Low priority)
			}
			if (track == null) {
				//Track doesn't exist
				player.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("general.cmd.delete.exists"));
				return;
			}
			toJoin = new RaceQueue(track, type, player);
			added = true;
		}
		// Join that queue
		if (!added) {
			toJoin.addPlayer(player);
		}
		toJoin.broadcast(MarioKart.colors.getTitle() + "[MarioKart:] "
				+ MarioKart.colors.getInfo() + player.getName()
				+ MarioKart.msgs.get("race.que.joined") + " ["
				+ toJoin.playerCount() + "/" + toJoin.playerLimit() + "]");
		executeLobbyJoin(player, toJoin);
		recalculateQueues();
		return;
	}

	public void joinQueue(Player player, RaceTrack track, RaceType type) {
		if(lockdown){
			player.sendMessage(MarioKart.colors.getError()+MarioKart.msgs.get("error.memoryLockdown"));
			return;
		}
		Boolean added = false;
		Map<UUID, RaceQueue> queues = MarioKart.plugin.raceQueues.getQueues(track.getTrackName(), type); // Get the oldest queue of that type for that track
		RaceQueue queue = null;
		if (queues.size() < 1) {
			queue = new RaceQueue(track, type, player);
			added = true;
		} else {
			for(UUID id:queues.keySet()){
				RaceQueue q = queues.get(id);
				if(q.playerCount() < q.playerLimit()){
					queue = q;
				}
			}
			if(queue == null){ //No queues of that type available, so create and schedule a new one
				queue = new RaceQueue(track, type, player);
				player.sendMessage(MarioKart.colors.getInfo()
						+ MarioKart.msgs.get("general.cmd.overflow"));
				added = true;
			}
		}
		if(!added){
			queue.addPlayer(player);
			added = true;
		}
		queue.broadcast(MarioKart.colors.getTitle() + "[MarioKart:] "
				+ MarioKart.colors.getInfo() + player.getName()
				+ MarioKart.msgs.get("race.que.joined") + " [" + queue.playerCount()
				+ "/" + queue.playerLimit() + "]");
		executeLobbyJoin(player, queue);
		recalculateQueues();
		return;
	}

	public void executeLobbyJoin(Player player, RaceQueue queue) {
		Location l = queue.getTrack().getLobby(MarioKart.plugin.getServer());
		Chunk chunk = l.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
		}
		player.teleport(l);
		String rl = MarioKart.plugin.packUrl;
		player.sendMessage(MarioKart.colors.getInfo()
				+ MarioKart.msgs.get("resource.download"));
		String msg = MarioKart.msgs.get("resource.downloadHelp");
		msg = msg.replaceAll(Pattern.quote("%url%"),
				Matcher.quoteReplacement(ChatColor.RESET + ""));
		player.sendMessage(MarioKart.colors.getInfo() + msg);
		player.sendMessage(rl); //new line
		//TODO Use setResourePack, but would remove old version support
		if(!MarioKart.plugin.resourcedPlayers.contains(player.getName()) 
				&& MarioKart.plugin.fullPackUrl != null
				&& MarioKart.plugin.fullPackUrl.length() > 0){
			player.setTexturePack(MarioKart.plugin.fullPackUrl);
			MarioKart.plugin.resourcedPlayers.add(player.getName());
		}
		return;
	}

	public synchronized void leaveQueue(Player player, RaceQueue queue) {
		queue.removePlayer(player);
		return;
	}

	public void recalculateQueues() {
		MarioKart.plugin.signManager.updateSigns();
		
		if(lockdown){
			//No more races allowed
			if(getRacesRunning() < 1){
				//Need to recall recalculateQueues else all will freeze
				MarioKart.plugin.getServer().getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

					@Override
					public void run() {
						recalculateQueues();
						return;
					}}, 600l);
			}
			return;
		}
		if (getRacesRunning() >= raceLimit) {
			MarioKart.logger.info("[INFO] Max races running");
			return; // Cannot start any more races for now...
		}
		Map<UUID, RaceQueue> queues = MarioKart.plugin.raceQueues.getAllQueues();
		if(queues.size() < 1){
			return;
		}
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
					MarioKart.logger.info("Delayed re-queueing due to lack of server resources!");
					if(getRacesRunning() < 1){
						MarioKart.plugin.getServer().getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){
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
				final List<Player> q = new ArrayList<Player>(queue.getPlayers());
				Bukkit.getScheduler().runTask(MarioKart.plugin, new Runnable(){

					@Override
					public void run() {
						for (Player p : q) {
							if (p != null && p.isOnline()
									&& getRacesRunning() < raceLimit) {
								Race race = new Race(queue.getTrack(),
										queue.getTrackName(), RaceType.TIME_TRIAL);
								race.join(p);
								if (race.getUsers().size() > 0) {
									startRace(race.getTrackName(), race);
								}
								queue.removePlayer(p);
							}
						}
						if (queue.playerCount() < 1) {
							q.clear();
							MarioKart.plugin.raceQueues.removeQueue(queue);
						}
						return;
					}});
			} else if (queue.playerCount() >= queue.getTrack().getMinPlayers()
					&& !isTrackInUse(queue.getTrack(), queue.getRaceMode())
					&& getRacesRunning() < raceLimit
					&& !queuedTracks.contains(queue.getTrack()) // Check it's
																// not reserved
					&& queue.getRaceMode() != RaceType.TIME_TRIAL
					&& !queue.isStarting()) {
				int c = queue.playerCount();
				double predicted = c*60+50; //Predicted Memory needed
				if(DynamicLagReducer.getResourceScore(predicted) < 30){
					MarioKart.logger.info("Delayed re-queueing due to lack of server resources!");
					if(getRacesRunning() < 1){
						MarioKart.plugin.getServer().getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){
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
				double graceS = MarioKart.config
						.getDouble("general.raceGracePeriod");
				long grace = (long) (graceS * 20);
				String msg = MarioKart.msgs.get("race.que.players");
				msg = msg.replaceAll(Pattern.quote("%time%"), "" + graceS);
				queue.broadcast(MarioKart.colors.getInfo() + msg);
				MarioKart.plugin.getServer().getScheduler()
						.runTaskLater(MarioKart.plugin, new Runnable() {

							@Override
							public void run() {
								if (queue.playerCount() < queue.getTrack().getMinPlayers()) {
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
								if (race.getUsers().size() >= queue.getTrack().getMinPlayers()) {
									queue.clear();
									MarioKart.plugin.raceQueues.removeQueue(queue);
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
				MarioKart.logger.info("[INFO] Max races running");
				return; // No more races can be run for now
			}
		}
	}

	private synchronized void putRace(Race race){
		this.races.put(race.getGameId(), race);
	}
	
	public void startRace(String trackName, final Race race) {
		putRace(race);
		final List<User> users = race.getUsers();
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
			grid.put(i, s.getLocation(MarioKart.plugin.getServer()).clone());
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
				int randomNumber = MarioKart.plugin.random.nextInt(max);
				User user = users.get(randomNumber);
				try {
					p = users.get(randomNumber).getPlayer();
				} catch (PlayerQuitException e) {
					// Player has left
				}
				users.remove(user);
				Location loc = grid.get(i);
				if (race.getType() == RaceType.TIME_TRIAL) {
					loc = grid.get(MarioKart.plugin.random.nextInt(grid.size()));
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
							MarioKart.plugin));
					car.setMetadata("kart.racing", new StatValue(null,
							MarioKart.plugin));
					car.setPassenger(p);
					p.setMetadata("car.stayIn",
							new StatValue(null, MarioKart.plugin));
					cars.add(car);
					if(fairCars){
						uCarsAPI.getAPI().setUseRaceControls(car.getUniqueId(), MarioKart.plugin);
					}
				}
			}
		}
		if (users.size() > 0) {
			User user = users.get(0);
			try {
				Player p = user.getPlayer();
				p.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("race.que.full"));
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
				player.sendMessage(MarioKart.colors.getInfo()
						+ MarioKart.msgs.get("race.que.preparing"));
			} catch (PlayerQuitException e) {
				// Player has left
			}
		}
		final List<User> users2 = race.getUsers();
		for (User user2 : users2) {
			user2.setInRace(true);
		}
		MarioKart.plugin.getServer().getScheduler()
				.runTaskAsynchronously(MarioKart.plugin, new Runnable() {
					@Override
					public void run() {
						for (User user : users2) {
							try {
								user.getPlayer()
										.sendMessage(
												MarioKart.colors.getInfo()
														+ MarioKart.msgs
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
													MarioKart.plugin.musicManager.playCustomSound(p, MarioKartSound.RACE_START_COUNTDOWN);
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
													MarioKart.plugin.musicManager.playCustomSound(p, MarioKartSound.COUNTDOWN_PLING);
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
									p.sendMessage(MarioKart.colors.getInfo() + ""
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
							car.removeMetadata("car.frozen", MarioKart.plugin);
						}
						for (User user : users2) {
							try {
								user.getPlayer().sendMessage(
										MarioKart.colors.getInfo()
												+ MarioKart.msgs.get("race.que.go"));
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
		return;
	}

	@Deprecated
	public synchronized void updateRace(Race race) {
		if(race == null || race.getGameId() == null){
			return;
		}
		if(this.races == null){
			this.races = new HashMap<UUID, Race>();
		}
		if (this.races.containsKey(race.getGameId())) {
			this.races.put(race.getGameId(), race);
		}
	}
	
	public synchronized void lockdown(){
		//Running out of system memory!
		this.lockdown = true;
		MarioKart.logger.info("[WANRING] Memory resources low, MarioKart has locked down all queues "
				+ "and may start to terminate races if condition persists!");
		return;
	}
	
	public boolean isLockedDown(){
		return this.lockdown;
	}
	
	public synchronized void unlockDown(){
		//System regained necessary memory
		this.lockdown = false;
		MarioKart.logger.info("[INFO] System memory stable once more, MarioKart has unlocked all queues!");
		return;
	}

	public HashMap<UUID, Race> getRaces() {
		return new HashMap<UUID, Race>(races);
	}

	public synchronized int getRacesRunning() {
		return races.size();
	}

	public synchronized Boolean isTrackInUse(RaceTrack track, RaceType type) {
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

	public synchronized Race inAGame(Player player, Boolean update) {
		Map<UUID, Race> races = MarioKart.plugin.raceScheduler.getRaces();
		for (UUID id : new ArrayList<UUID>(races.keySet())) {
			Race r = races.get(id);
			if (update) {
				r.updateUser(player);
			}
			List<User> users = r.getUsersIn(); // Exclude those that have
												// finished the race
			for (User u : users) {
				if (u.getPlayerName().equals(player.getName())) {
					return r;
				}
			}
		}
		return null;
	}
	
	public synchronized void endAll(){
		for (UUID id : races.keySet()) {
			races.get(id).end(); // End the race
		}
	}
}
