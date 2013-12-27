package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.stormdev.mario.utils.CheckpointCheck;
import net.stormdev.mario.utils.DoubleValueComparator;
import net.stormdev.mario.utils.MarioKartRaceFinishEvent;
import net.stormdev.mario.utils.MarioKartSound;
import net.stormdev.mario.utils.PlayerQuitException;
import net.stormdev.mario.utils.RaceType;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.ucarsCommon.StatValue;

public class RaceExecutor {
	public static void onRaceEnd(Race game) {
		if (game == null) {
			return;
		}
		game.running = false;
		try {
			game.users.clear();
		} catch (Exception e2) {
			// Users already cleared
		}
		try {
			if (main.plugin.raceScheduler.isTrackInUse(game.getTrack(),
					game.getType())) {
				main.plugin.raceScheduler.removeRace(game);
			}
		} catch (Exception e1) {
			try {
				main.plugin.raceScheduler.removeRace(game);
			} catch (Exception e) {
				// Nothing
			}
		}
		main.plugin.raceScheduler.recalculateQueues();
		if (!game.isEmpty()) {
			main.logger.info("MEMORY LEAK ALERT");
		}
	}

	public static synchronized void finishRace(Race game, User user) {
		try {
			Boolean timed = game.getType() == RaceType.TIME_TRIAL;
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
				player = main.plugin.getServer()
						.getPlayer(user.getPlayerName());
				if (player == null || !player.isOnline()) {
					try {
						game.leave(user, true);
					} catch (Exception e) {} //Ignore if race has been ended already
					return; // Player is no longer around...
				}
			}
			if (player != null) {
				player.removeMetadata("car.stayIn", main.plugin);
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
				Location loc = game.getTrack().getExit(main.plugin.getServer());
				if (loc == null) {
					player.teleport(player.getLocation().getWorld()
							.getSpawnLocation());
				} else {
					player.teleport(loc);
				}
				if (player.isOnline()) {
					player.getInventory().clear();

					player.getInventory().setContents(user.getOldInventory());
				}
			}
			if (game.isUserFinished(user.getPlayerName())) {
				finished = true;
			} else {
				HashMap<User, Double> checkpointDists = new HashMap<User, Double>();
				for (User u : game.getUsers()) {
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

				for (User u : game.getUsers()) {
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
				DoubleValueComparator com = new DoubleValueComparator(scores);
				SortedMap<String, Double> sorted = new TreeMap<String, Double>(
						com);
				sorted.putAll(scores);
				Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				for (int i = 0; i < pls.length; i++) {
					Player p = main.plugin.getServer().getPlayer(
							(String) pls[i]); // Evidence the dodgy PR was not
												// tested as it was still
												// reading string with Player in
												// the map
					if (p.equals(player)) {
						if (p != null) {
							String msg = "";
							if (!timed) {
								//Normal race, or cup
								msg = main.msgs.get("race.end.position");
								if ((i + 1) <= 4
										&& (i + 1) != game.howManyPlayers()) {
									//Winning sound
									main.plugin.playCustomSound(player, MarioKartSound.RACE_WIN);
								} else {
									//Lose sound
									main.plugin.playCustomSound(player, MarioKartSound.RACE_LOSE);
								}
								i = i + game.getFinishPosition(user.getPlayerName());
								String pos = "" + i;
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
										player, (i + 1), pos);
								main.plugin.getServer().getPluginManager()
										.callEvent(evt);
							} else {
								//Time trial
								double tim = (game.endTimeMS - game.startTimeMS) / 10;
								double ti = (int) tim;
								double t = ti / 100;
								msg = main.msgs.get("race.end.time");
								msg = msg.replaceAll(Pattern.quote("%time%"), t
										+ "");
								main.plugin.playCustomSound(player, MarioKartSound.RACE_WIN);
								main.plugin.raceTimes.addRaceTime(game
										.getTrack().getTrackName(), player
										.getName(), t);
							}
							p.sendMessage(main.colors.getSuccess() + msg);
						}
					}
				}
			} else {
				if (player != null) {
					int position = game.getFinishPosition(user.getPlayerName());
					String msg = "";
					if (!timed) {
						msg = main.msgs.get("race.end.position");
						if (position <= 4 && position != game.howManyPlayers()) {
							//Win sound
							main.plugin.playCustomSound(player, MarioKartSound.RACE_WIN);
						} else {
							//Lose sound
							main.plugin.playCustomSound(player, MarioKartSound.RACE_LOSE);
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
								player, position, pos);
						main.plugin.getServer().getPluginManager()
								.callEvent(evt);
					} else {
						// Time trial
						double tim = (game.endTimeMS - game.startTimeMS) / 10;
						double ti = (int) tim;
						double t = ti / 100;
						msg = main.msgs.get("race.end.time");
						msg = msg.replaceAll(Pattern.quote("%time%"), t + "");
						main.plugin.playCustomSound(player, MarioKartSound.RACE_WIN);
						main.plugin.raceTimes.addRaceTime(game.getTrack()
								.getTrackName(), player.getName(), t);
					}
					player.sendMessage(main.colors.getSuccess() + msg);
				}
			}
			game.leave(user, false);
			main.plugin.raceScheduler.updateRace(game);
			if (game.howManyPlayersIn() < 1) {
				game.ended = true;
				game.ending = true;
				game.end();
			}
			return;
		} catch (Exception e) {
			// Player has left
			return;
		}
	}
	@SuppressWarnings("deprecation")
	public static synchronized void onRaceStart(Race game){
		List<User> users = game.getUsers();
		for (User user : users) {
			try {
				Player player = user.getPlayer();
				player.setGameMode(GameMode.SURVIVAL);
				player.getInventory().clear();
				main.plugin.hotBarManager.updateHotBar(player);
				player.updateInventory();
			} catch (PlayerQuitException e) {
				// Player has left
				game.leave(user, true);
			}
		}
		main.plugin.raceScheduler.updateRace(game);
		users = game.getUsers();
		for (User user : users) {
			user.setLapsLeft(game.totalLaps);
			user.setCheckpoint(0);
			String msg = main.msgs.get("race.mid.lap");
			msg = msg.replaceAll(Pattern.quote("%lap%"), "" + 1);
			msg = msg.replaceAll(Pattern.quote("%total%"), "" + game.totalLaps);
			try {
				user.getPlayer().sendMessage(main.colors.getInfo() + msg);
			} catch (PlayerQuitException e) {
				// Player has left
			}
		}
		game.setUsers(users);
		main.plugin.raceScheduler.recalculateQueues();
		return;
	}
	public static void onRaceUpdate(final Race game){
		if (!game.getRunning()) {
			try {
				main.plugin.raceScheduler.stopRace(game);
			} catch (Exception e) {
			}
			main.plugin.raceScheduler.recalculateQueues();
			return;
		}
		if (!game.ending
				&& !game.ending
				&& main.config.getBoolean("general.race.enableTimeLimit")
				&& ((System.currentTimeMillis() - game.startTimeMS) * 0.001) > game.timeLimitS) {
			game.broadcast(main.msgs.get("race.end.timeLimit"));
			game.ending = true;
			game.end();
			return;
		}
		for (User user : game.getUsersIn()) {
			String pname = user.getPlayerName();
			Player player = main.plugin.getServer().getPlayer(pname);
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
							player, main.plugin.getServer());

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
						if (game.atLine(main.plugin.getServer(), playerLoc)) {
							if (checkNewLap) {
								int left = lapsLeft - 1;
								if (left < 0) {
									left = 0;
								}
								user.setCheckpoint(0);
								user.setLapsLeft(left);
								lapsLeft = left;
								if (left != 0) {
									String msg = main.msgs.get("race.mid.lap");
									int lap = game.totalLaps - lapsLeft + 1;
									msg = msg.replaceAll(Pattern.quote("%lap%"), ""
											+ lap);
									msg = msg.replaceAll(Pattern.quote("%total%"),
											"" + game.totalLaps);
									if (lap == game.totalLaps) {
										//Last lap
										main.plugin.playCustomSound(player, MarioKartSound.LAST_LAP);
									}
									player.sendMessage(main.colors.getInfo() + msg);
								}
							}
							if (lapsLeft < 1) {
								Boolean won = game.getWinner() == null;
								if (won) {
									game.setWinner(user);
								}
								game.finish(user);
								if (won && game.getType() != RaceType.TIME_TRIAL) {
									String msg = main.msgs
											.get("race.end.soon");
									game.broadcast(main.colors.getSuccess()
													+ game.getWinner()
													+ main.msgs.get("race.end.won"));
									game.broadcast(main.colors.getInfo()
											        + msg);
								}
							}
						}
					}
				}
			}
		}
		main.plugin.raceScheduler.updateRace(game);
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
		car.setMetadata("car.frozen", new StatValue(time, main.plugin));
		car.setVelocity(new Vector(0, power, 0));
		final Player pl = player;
		main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable() {

			public void run() {
				main.plugin.playCustomSound(pl, MarioKartSound.PENALTY_END);
				car.removeMetadata("car.frozen", main.plugin);
			}
		}, (time * 20));
		return;
	}

}
