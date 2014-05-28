package net.stormdev.mario.races;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.PlayerQuitException;
import net.stormdev.mario.players.User;
import net.stormdev.mario.sound.MarioKartSound;
import net.stormdev.mario.utils.DoubleValueComparator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.useful.ucarsCommon.StatValue;

public class RaceExecutor {
	public static void onRaceEnd(Race game) {
		if (game == null) {
			return;
		}
		game.running = false;
		try {
			game.clearUsers();
		} catch (Exception e2) {
			// Users already cleared
		}
		if (!game.isEmpty()) {
			MarioKart.logger.info("Game not correctly cleared!");
		}
		MarioKart.plugin.raceScheduler.recalculateQueues();
		MarioKartRaceEndEvent evt = new MarioKartRaceEndEvent(game);
		Bukkit.getPluginManager().callEvent(evt);
		return;
	}

	public static void finishRace(final Race game, final User user, final Boolean gameEnded){
		//Call finishRaceSync, syncrhonously
		MarioKart.plugin.getServer().getScheduler().runTaskLater(MarioKart.plugin, new BukkitRunnable(){

			@Override
			public void run() {
				finishRaceSync(game, user, gameEnded);
				return;
			}}, 2l);
	}
	
	public static void finishRaceSync(Race game, final User user, Boolean gameEnded) {
		try {
			Boolean timed = game.getType() == RaceType.TIME_TRIAL;
			List<User> usersIn = game.getUsersIn();
			String in = "";
			for (User us : usersIn) {
				in = in + ", " + us.getPlayerName();
			}
			Map<String, Double> scores = new HashMap<String, Double>();
			Boolean finished = false;
			Player player = null;
			try {
				player = user.getPlayer();
			} catch (PlayerQuitException e1) {
				// Player has left
			}
			if (player == null) {
				// Player has been removed from race prematurely
				player = MarioKart.plugin.getServer()
						.getPlayer(user.getPlayerName());
				if (player == null || !player.isOnline()) {
					return; // Player is no longer around...
				}
			}
			if (player != null) {
				player.removeMetadata("car.stayIn", MarioKart.plugin);
				player.setCustomName(ChatColor.stripColor(player
						.getCustomName()));
				player.setCustomNameVisible(false);
				if (player.getVehicle() != null) {
					Entity e = player.getVehicle();
					List<Entity> stack = new ArrayList<Entity>();
					while(e!=null){
						stack.add(e);
						e = e.getVehicle();
					}
					for(Entity veh:stack){
						veh.eject();
						veh.remove();
					}
				}
				
				final Location loc = game.getTrack().getExit(MarioKart.plugin.getServer());
				final Player pl = player;
				
				MarioKart.plugin.getServer().getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

					@Override
					public void run() {
						if (loc == null) {
							pl.teleport(pl.getLocation().getWorld()
									.getSpawnLocation());
						} else {
							pl.teleport(loc);
						}
						if (pl.isOnline()) {
							pl.getInventory().clear();

							pl.getInventory().setContents(user.getOldInventory());
							pl.setGameMode(user.getOldGameMode());
						}
						return;
					}}, 4l);
			}
			if (game.finished.contains(user.getPlayerName())) {
				finished = true;
			} else {
				HashMap<User, Double> checkpointDists = new HashMap<User, Double>();
				for (User u : game.getUsersIn()) {
					try {
						Player pp = u.getPlayer();
						if (pp != null) {
							if (pp.hasMetadata("checkpoint.distance")) {
								List<MetadataValue> metas = pp
										.getMetadata("checkpoint.distance");
								checkpointDists.put(u,
										(Double) ((StatValue) metas.get(0))
												.getValue());
							}
						}
					} catch (PlayerQuitException e) {
						// Player has left
					}
				}

				for (User u : game.getUsersIn()) {
					try {
						int laps = game.totalLaps - u.getLapsLeft() + 1;

						int checkpoints = u.getCheckpoint();

						double distance = 1 / (checkpointDists.get(u));

						double score = (laps * game.getMaxCheckpoints())
								+ checkpoints + distance;

						try {
							if (game.getWinner().equals(u)) {
								score = score + 1;
							}
						} catch (Exception e) {
						}
						scores.put(u.getPlayerName(), score);
					} catch (Exception e) {
						// User has left
					}
				}
			}
			if (player != null) {
				player.getInventory().clear();

				player.getInventory().setContents(user.getOldInventory());
			}
			if (!finished) {
				//Auto finish
				DoubleValueComparator com = new DoubleValueComparator(scores);
				SortedMap<String, Double> sorted = new TreeMap<String, Double>(
						com);
				sorted.putAll(scores);
				Set<String> keys = sorted.keySet();
				Object[] pls = keys.toArray();
				for (int i = 0; i < pls.length; i++) {
					Player p = MarioKart.plugin.getServer().getPlayer(
							(String) pls[i]); // Evidence the dodgy PR was not
												// tested as it was still
												// reading string with Player in
												// the map
					if (p.equals(player)) {
						if (p != null) {
							String msg = "";
							if (!timed) {
								//Normal race, or cup
								msg = MarioKart.msgs.get("race.end.position");
								if ((i + 1) <= 4
										&& (i + 1) != game.getUsers().size()) {
									//Winning sound
									MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.RACE_WIN);
								} else {
									//Lose sound
									MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.RACE_LOSE);
								}
								i += game.getUsersFinished().size();
								String pos = "" + (i + 1);
								if (pos.endsWith("1")) {
									pos = pos + "st";
								} else if (pos.endsWith("2")) {
									pos = pos + "nd";
								} else if (pos.endsWith("3")) {
									pos = pos + "rd";
								} else {
									pos = pos + "th";
								}
								msg = msg.replaceAll("%position%", "" + pos);
								MarioKartRaceFinishEvent evt = new MarioKartRaceFinishEvent(
										player, (i + 1), pos, game.getTrack().getRewardConfig());
								MarioKart.plugin.getServer().getPluginManager()
										.callEvent(evt);
							} else {
								//Time trial
								double tim = (game.endTimeMS - game.startTimeMS) / 10;
								double ti = (int) tim;
								double t = ti / 100;
								msg = MarioKart.msgs.get("race.end.time");
								msg = msg.replaceAll(Pattern.quote("%time%"), t
										+ "");
								MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.RACE_WIN);
								if(!gameEnded){
									MarioKart.plugin.raceTimes.addRaceTime(game
											.getTrack().getTrackName(), player
											.getName(), t);
								}
							}
							p.sendMessage(MarioKart.colors.getSuccess() + msg);
						}
					}
				}
			} else {
				//Finish as is they-crossed-the-line
				if (player != null) {
					int position = game.getFinishPosition(player.getName());
					String msg = "";
					if (!timed) {
						msg = MarioKart.msgs.get("race.end.position");
						if (position <= 4 && position != game.getUsers().size()) {
							//Win sound
							MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.RACE_WIN);
						} else {
							//Lose sound
							MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.RACE_LOSE);
						}
						String pos = "" + position;
						if (pos.endsWith("1")) {
							pos = pos + "st";
						} else if (pos.endsWith("2")) {
							pos = pos + "nd";
						} else if (pos.endsWith("3")) {
							pos = pos + "rd";
						} else {
							pos = pos + "th";
						}
						try {
							msg = msg.replaceAll("%position%", "" + pos);
						} catch (Exception e) {
						}
						MarioKartRaceFinishEvent evt = new MarioKartRaceFinishEvent(
								player, position, pos, game.getTrack().getRewardConfig());
						MarioKart.plugin.getServer().getPluginManager()
								.callEvent(evt);
					} else {
						// Time trial
						double tim = (game.endTimeMS - game.startTimeMS) / 10;
						double ti = (int) tim;
						double t = ti / 100;
						msg = MarioKart.msgs.get("race.end.time");
						msg = msg.replaceAll(Pattern.quote("%time%"), t + "");
						MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.RACE_WIN);
						MarioKart.plugin.raceTimes.addRaceTime(game.getTrack()
								.getTrackName(), player.getName(), t);
					}
					player.sendMessage(MarioKart.colors.getSuccess() + msg);
				}
			}
			game.leave(user, false);
			if (game.getUsersIn().size() < 1 && !game.ended && !gameEnded) {
				game.ended = true;
				game.end();
			}
			return;
		} catch (Exception e) {
			// Player has left
			return;
		}
	}
	@SuppressWarnings("deprecation")
	public static void onRaceStart(Race game){
		List<User> users = game.getUsers();
		for (User user : users) {
			try {
				Player player = user.getPlayer();
				player.setGameMode(GameMode.SURVIVAL);
				player.getInventory().clear();
				MarioKart.plugin.hotBarManager.updateHotBar(player);
				player.updateInventory();
			} catch (PlayerQuitException e) {
				// Player has left
				game.leave(user, true);
			}
		}
		MarioKart.plugin.raceScheduler.updateRace(game);
		users = game.getUsers();
		for (User user : users) {
			user.setLapsLeft(game.totalLaps);
			user.setCheckpoint(0);
			String msg = MarioKart.msgs.get("race.mid.lap");
			msg = msg.replaceAll(Pattern.quote("%lap%"), "" + 1);
			msg = msg.replaceAll(Pattern.quote("%total%"), "" + game.totalLaps);
			try {
				user.getPlayer().sendMessage(MarioKart.colors.getInfo() + msg);
			} catch (PlayerQuitException e) {
				// Player has left
			}
		}
		game.setUsers(users);
		MarioKart.plugin.raceScheduler.recalculateQueues();
		return;
	}
	public static void onRaceUpdate(final Race game){
		if (!game.getRunning()) {
			try {
				MarioKart.plugin.raceScheduler.stopRace(game);
			} catch (Exception e) {
			}
			MarioKart.plugin.raceScheduler.recalculateQueues();
			return;
		}
		if (!game.ending
				&& !game.ending
				&& MarioKart.config.getBoolean("general.race.enableTimeLimit")
				&& ((System.currentTimeMillis() - game.startTimeMS) * 0.001) > game.timeLimitS) {
			game.broadcast(MarioKart.msgs.get("race.end.timeLimit"));
			game.ending = true;
			game.end();
			return;
		}
		for (User user : game.getUsersIn()) {
			String pname = user.getPlayerName();
			Player player = MarioKart.plugin.getServer().getPlayer(pname);
			if (player == null && !user.isRespawning()) {
				game.leave(user, true);
			} else {
				if(player != null){
					Location playerLoc = player.getLocation();
					Boolean checkNewLap = false;
					int old = user.getCheckpoint();
					if (old == game.getMaxCheckpoints()) {
						checkNewLap = true;
					}
					Integer[] toCheck = new Integer[] {};
					if (checkNewLap) {
						toCheck = new Integer[] { 0 };
					} else {
						toCheck = new Integer[] { (old + 1) };
					}
					CheckpointCheck check = game.playerAtCheckpoint(toCheck,
							player, MarioKart.plugin.getServer());

					if (check.at) { // At a checkpoint
						int ch = check.checkpoint;
						if (ch >= game.getMaxCheckpoints()) {
							checkNewLap = true;
						}
						if (!(ch == old)) {
							/*
							 * Removed to reduce server load - Requires all
							 * checkpoints to be checked if(ch-2 > old){ //They
							 * missed a checkpoint
							 * player.sendMessage(main.colors.getError
							 * ()+main.msgs.get("race.mid.miss")); return; }
							 */
							if (!(old >= ch)) {
								user.setCheckpoint(check.checkpoint);
							}
						}
					}
					int lapsLeft = user.getLapsLeft();

					if (lapsLeft < 1 || checkNewLap) {
						if (game.atLine(MarioKart.plugin.getServer(), playerLoc)) {
							if (checkNewLap) {
								int left = lapsLeft - 1;
								if (left < 0) {
									left = 0;
								}
								user.setCheckpoint(0);
								user.setLapsLeft(left);
								lapsLeft = left;
								if (left != 0) {
									String msg = MarioKart.msgs.get("race.mid.lap");
									int lap = game.totalLaps - lapsLeft + 1;
									msg = msg.replaceAll(Pattern.quote("%lap%"), ""
											+ lap);
									msg = msg.replaceAll(Pattern.quote("%total%"),
											"" + game.totalLaps);
									if (lap == game.totalLaps) {
										//Last lap
										MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.LAST_LAP);
									}
									player.sendMessage(MarioKart.colors.getInfo() + msg);
								}
							}
							if (lapsLeft < 1) {
								Boolean won = game.getWinner() == null;
								if (won) {
									game.setWinner(user);
								}
								game.finish(user);
								if (won && game.getType() != RaceType.TIME_TRIAL) {
									for (User u : game.getUsers()) {
										Player p;
										try {
											p = u.getPlayer();
											String msg = MarioKart.msgs
													.get("race.end.soon");
											msg = msg.replaceAll("%name%",
													p.getName());
											p.sendMessage(MarioKart.colors.getSuccess()
													+ game.getWinner()
													+ MarioKart.msgs.get("race.end.won"));
											p.sendMessage(MarioKart.colors.getInfo()
													+ msg);
										} catch (PlayerQuitException e) {
											// Player has left
										} catch (Exception e){
											//Player is respawning
										}

									}
								}
							}
						}
					}
				}
			}
		}
		return;
	}
	
	public static void penalty(Player player, final Minecart car, long time) {
		if (car == null) {
			return;
		}
		if (car.hasMetadata("kart.immune")) {
			return;
		}
		double power = (time / 2);
		if (power < 1) {
			power = 1;
		}
		car.setMetadata("car.frozen", new StatValue(time, MarioKart.plugin));
		car.setVelocity(new Vector(0, power, 0));
		final Player pl = player;
		MarioKart.plugin.getServer().getScheduler().runTaskLater(MarioKart.plugin, new Runnable() {

			@Override
			public void run() {
				MarioKart.plugin.musicManager.playCustomSound(pl, MarioKartSound.PENALTY_END);
				car.removeMetadata("car.frozen", MarioKart.plugin);
			}
		}, (time * 20));
		return;
	}

}
