package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import net.stormdev.mario.utils.CheckpointCheck;
import net.stormdev.mario.utils.DoubleValueComparator;
import net.stormdev.mario.utils.DynamicLagReducer;
import net.stormdev.mario.utils.PlayerQuitException;
import net.stormdev.mario.utils.RaceTrack;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.useful.ucarsCommon.StatValue;

public class Race {
	public List<String> finished = new ArrayList<String>();
	public List<User> users = new ArrayList<User>();
	private UUID gameId = null;
	private RaceTrack track = null;
	private String trackName = "";
	private String winner = null;
	public String winning = "";
	public Boolean running = false;
	public long startTimeMS = 0;
	public long endTimeMS = 0;
	public long timeLimitS = 0;
	public long tickrate = 6;
	public long scorerate = 15;
	private BukkitTask task = null;
	private BukkitTask scoreCalcs = null;
	public int maxCheckpoints = 3;
	public int totalLaps = 3;
	public ArrayList<Location> reloadingItemBoxes = new ArrayList<Location>();
	public int finishCountdown = 60;
	Boolean ending = false;
	Boolean ended = false;
	public Scoreboard board = null;
	public Objective scores = null;
	public RaceType type = RaceType.RACE;
	public Objective scoresBoard = null;

	public Race(RaceTrack track, String trackName, RaceType type) {
		this.type = type;
		this.gameId = UUID.randomUUID();
		this.track = track;
		this.trackName = trackName;
		this.totalLaps = this.track.getLaps();
		this.maxCheckpoints = this.track.getCheckpoints().size() - 1;
		this.tickrate = main.config.getLong("general.raceTickrate");
		this.scorerate = (long) ((this.tickrate * 2) + (this.tickrate / 0.5));
		this.board = main.plugin.getServer().getScoreboardManager()
				.getNewScoreboard();
		this.scores = board.registerNewObjective("", "dummy");
		scores.setDisplaySlot(DisplaySlot.BELOW_NAME);
		if (type != RaceType.TIME_TRIAL) {
			this.scoresBoard = board.registerNewObjective(ChatColor.GOLD
					+ "Race Positions", "dummy");
		} else { // Time Trial
			this.scoresBoard = board.registerNewObjective(ChatColor.GOLD
					+ "Race Time(s)", "dummy");
		}
		scoresBoard.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.timeLimitS = main.config
				.getInt("general.race.maxTimePerCheckpoint")
				* track.getCheckpoints().size() + 60;
	}

	public RaceType getType() {
		return this.type;
	}

	public User getUser(Player player) {
		String pname = player.getName();
		for (User user : getUsers()) {
			try {
				if (user.getPlayerName().equals(pname)) {
					return user;
				}
			} catch (Exception e) {
				if (!forceRemoveUser(user)) {
					main.logger.info("getUser() failed to remove user");
				}
			}
		}
		return null;
	}

	public User getUser(String playerName) {
		for (User user : getUsers()) {
			if (user.getPlayerName().equals(playerName)) {
				return user;
			}
		}
		return null;
	}

	public List<User> getUsers() {
		return new ArrayList<User>(users);
	}

	public void setUsers(List<User> users) {
		this.users = users;
		return;
	}

	public List<User> getUsersIn() {
		List<User> inUsers = new ArrayList<User>();
		for (User user : getUsers()) {
			if (user.isInRace()) {
				inUsers.add(user);
			}
		}
		return inUsers;
	}

	public List<String> getUsersFinished() {
		return new ArrayList<String>(finished);
	}

	public void playerOut(User user) {
		user.setInRace(false);
		main.plugin.hotBarManager.clearHotBar(user.getPlayerName());
		Player player = null;
		try {
			player = user.getPlayer();
		} catch (PlayerQuitException e) {
			if (!forceRemoveUser(user)
					&& playerUserRegistered(user.getPlayerName())) {
				main.logger.info("race.playerOut failed to remove user");
			}
			return;
		}
		player.setLevel(user.getOldLevel());
		player.setExp(user.getOldExp());
		return;
	}

	public Boolean join(Player player) {
		if (users.size() < this.track.getMaxPlayers()) {
			User user = new User(player, player.getLevel(), player.getExp());
			users.add(user);
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void leave(User user, boolean quit) {
		Player player = null;
		try {
			player = user.getPlayer();
			player.setLevel(user.getOldLevel());
		} catch (PlayerQuitException e1) {
			// User quit
		}
		if (quit) {
			if (!forceRemoveUser(user)) {
				main.logger.info("race.quit failed to remove user");
			}
			if (type != RaceType.TIME_TRIAL) {
				if (users.size() < 2) {
					for (User u : getUsersIn()) {
						String msg = main.msgs.get("race.end.soon");
						try {
							u.getPlayer().sendMessage(
									main.colors.getInfo() + msg);
						} catch (PlayerQuitException e) {
							// Player is no longer in the game
							if (!forceRemoveUser(u)) {
								main.logger
										.info("race.leave failed to remove user");
							}
						}
					}
					startEndCount();
				}
			}
		}
		playerOut(user);
		if (player != null) {
			player.removeMetadata("car.stayIn", main.plugin);
		}
		if (quit) {
			if (player != null) {
				scoresBoard.getScore(player).setScore(0);
				this.board.resetScores(player);
				player.getInventory().clear();
				if (player.getVehicle() != null) {
					Vehicle veh = (Vehicle) player.getVehicle();
					veh.eject();
					veh.remove();
				}
				player.removeMetadata("car.stayIn", main.plugin);
				player.getInventory().setContents(user.getOldInventory());
				player.setGameMode(GameMode.SURVIVAL);
				try {
					player.teleport(this.track.getExit(main.plugin.getServer()));
				} catch (Exception e) {
					player.teleport(player.getWorld().getSpawnLocation());
				}
				player.sendMessage(ChatColor.GOLD
						+ "Successfully quit the race!");
				player.setScoreboard(main.plugin.getServer()
						.getScoreboardManager().getMainScoreboard());
				player.updateInventory();
			}
			for (User us : getUsers()) {
				try {
					us.getPlayer().sendMessage(
							ChatColor.GOLD + player.getName()
									+ " quit the race!");
				} catch (PlayerQuitException e) {
					if (!forceRemoveUser(us)) {
						main.logger.info("race.quit failed to remove user");
					}
				}
			}
		}
		try {
			recalculateGame();
		} catch (Exception e) {
		}
		return;
	}

	public void recalculateGame() {
		if (getUsersIn().size() < 1) {
			this.running = false;
			this.ended = true;
			this.ending = true;
			try {
				end();
			} catch (Exception e) {
			}
			main.plugin.raceScheduler.recalculateQueues();
		}
	}

	public Boolean isEmpty() {
		if (getUsers().size() < 1) {
			return true;
		}
		return false;
	}

	public UUID getGameId() {
		return this.gameId;
	}

	public String getTrackName() {
		return this.trackName;
	}

	public RaceTrack getTrack() {
		return this.track;
	}

	public void setWinner(User winner) {
		this.winner = winner.getPlayerName();
	}

	public void startEndCount() {
		final int count = this.finishCountdown;

		main.plugin.getServer().getScheduler()
				.runTaskAsynchronously(main.plugin, new Runnable() {

					public void run() {
						int z = count;
						while (z > 0) {
							z--;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
						}
						if (!ended) {
							try {
								try {
									main.plugin
											.getServer()
											.getScheduler()
											.runTask(main.plugin,
													new Runnable() {

														public void run() {
															end();

															return;
														}
													});
								} catch (Exception e) {
									end();
								}
							} catch (IllegalArgumentException e) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e1) {
								}
								run();
								return;
							}
						}
						return;
					}
				});
	}

	public String getWinner() {
		return this.winner;
	}

	public Boolean getRunning() {
		return this.running;
	}

	public void start() {
		this.running = true;
		final Race game = this;
		for (User user : getUsersIn()) {
			try {
				user.getPlayer().setScoreboard(board);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (PlayerQuitException e) {
				// User has left
				this.leave(user, true);
			}
		}
		final long ms = tickrate * 50;
		this.task = main.plugin.getServer().getScheduler()
				.runTaskAsynchronously(main.plugin, new Runnable() {

					public void run() {
						long mis = ms;
						while (running && !ended) {
							double tps = DynamicLagReducer.getTPS();
							if (tps < 19.9) {
								if (tps < 13) {
									mis = (long) (ms + (tps * 100));
								} else if (tps < 15) {
									mis = ms + 1000; // Go all out to keep up
								} else if (tps < 17) {
									main.logger
											.info("[WARNING] Server running out of resources! - "
													+ "Compensating by reducing MarioKart tickRate (Accuracy)");
									mis = ms + 500; // Reduce lag
								} else if (tps < 19) {
									mis = ms + 100;
								} else {
									mis = ms + 10; // Slow down a tad
								}
								try {
									Thread.sleep(mis);
								} catch (InterruptedException e) {
									// Nothing
								}
							} else {
								try {
									Thread.sleep(ms);
								} catch (InterruptedException e) {
									// Nothing
								}
							}
							RaceExecutor.onRaceUpdate(game);
						}
						return;
					}
				});
		this.scoreCalcs = main.plugin.getServer().getScheduler()
				.runTaskTimer(main.plugin, new Runnable() {

					public void run() {
						if (!(type == RaceType.TIME_TRIAL)) {
							SortedMap<String, Double> sorted = game
									.getRaceOrder();
							Object[] keys = sorted.keySet().toArray();
							for (int i = 0; i < sorted.size(); i++) {
								int pos = i + 1;
								String pname = (String) keys[i];
								User u = getUser(pname);
								try {
									Player pl = u.getPlayer();
									game.scores.getScore(pl).setScore(pos);
									game.scoresBoard.getScore(pl)
											.setScore(-pos);
								} catch (IllegalStateException e) {
									e.printStackTrace();
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (PlayerQuitException e) {
									// Player has left
									if (!forceRemoveUser(u)) {
										main.logger
												.info("race.scores failed to remove invalid user");
									}
								}
							}
						} else { // Time trial
							User user = null;
							try {
								user = game.getUsers().get(0);
							} catch (Exception e1) {
								return; // No players
							}
							try {
								Player pl = user.getPlayer();
								long time = System.currentTimeMillis()
										- startTimeMS;
								time = time / 1000; // In s
								game.scores.getScore(pl).setScore((int) time);
								game.scoresBoard.getScore(pl).setScore(
										(int) time);
							} catch (Exception e) {
								return;
								// Game ended or user has left or random error
								// with above code
							}
						}
						return;
					}
				}, this.scorerate, this.scorerate);
		try {
			this.startTimeMS = System.currentTimeMillis();
			RaceExecutor.onRaceStart(this);
		} catch (Exception e) {
			main.logger.log("Error starting race!", Level.SEVERE);
			end();
		}
		return;
	}

	public SortedMap<String, Double> getRaceOrder() {
		Race game = this;
		HashMap<String, Double> checkpointDists = new HashMap<String, Double>();
		List<User> users = game.getUsers();
		for (User user : users) {
			try {
				Player player = user.getPlayer();
				if (player.hasMetadata("checkpoint.distance")) {
					List<MetadataValue> metas = player
							.getMetadata("checkpoint.distance");
					checkpointDists.put(user.getPlayerName(),
							(Double) ((StatValue) metas.get(0)).getValue());
				}
			} catch (PlayerQuitException e) {
				leave(user, true);
				// Player is no longer in the race
			}
		}
		Map<String, Double> scores = new HashMap<String, Double>();
		for (User user : users) {
			int laps = game.totalLaps - user.getLapsLeft() + 1;
			int checkpoints = user.getCheckpoint();
			double distance = 1 / (checkpointDists.get(user.getPlayerName()));

			double score = (laps * game.getMaxCheckpoints()) + checkpoints
					+ distance;
			try {
				if (game.getWinner().equals(user)) {
					score = score + 1;
				}
			} catch (Exception e) {
			}
			scores.put(user.getPlayerName(), score);
		}
		DoubleValueComparator com = new DoubleValueComparator(scores);
		SortedMap<String, Double> sorted = new TreeMap<String, Double>(com);
		sorted.putAll(scores);
		if (sorted.size() >= 1) {
			this.winning = (String) sorted.keySet().toArray()[0];
		}
		return sorted;
	}

	@SuppressWarnings("unchecked")
	public void end() {
		this.running = false;
		for (Location l : ((List<Location>) this.reloadingItemBoxes.clone())) {
			main.listener.spawnItemPickupBox(l.add(0, 2.4, 0), false);
			this.reloadingItemBoxes.remove(l);
		}
		if (task != null) {
			task.cancel();
		}
		try {
			if (scoreCalcs != null) {
				scoreCalcs.cancel();
			}
		} catch (Exception e1) {
		}
		try {
			this.board.clearSlot(DisplaySlot.BELOW_NAME);
		} catch (IllegalArgumentException e) {
		}
		try {
			this.board.clearSlot(DisplaySlot.SIDEBAR);
		} catch (IllegalArgumentException e) {
		}
		try {
			this.scores.unregister();
			this.scoresBoard.unregister();
		} catch (IllegalStateException e) {
		}

		this.endTimeMS = System.currentTimeMillis();
		for (User user : getUsersIn()) {
			Player player = null;
			try {
				player = user.getPlayer();

				player.setScoreboard(main.plugin.getServer()
						.getScoreboardManager().getMainScoreboard());

				player.setLevel(user.getOldLevel());

				player.setExp(user.getOldExp());
			} catch (PlayerQuitException e) {
				leave(user, true);
			}
			RaceExecutor.finishRace(this, user);
		}
		RaceExecutor.onRaceEnd(this);
		clear();
		main.plugin.raceScheduler.removeRace(this);
		main.plugin.raceScheduler.recalculateQueues();
	}

	public void finish(User user) {
		if (!ending) {
			ending = true;
			startEndCount();
		}
		finished.add(user.getPlayerName());
		if (!forceRemoveUser(user)) {
			main.logger.info("race.finish failed to remove user");
		}
		user.setFinished(true);
		user.setInRace(false);
		users.add(user);
		try {
			Player player = user.getPlayer();
			if (player == null) {
				player = main.plugin.getServer()
						.getPlayer(user.getPlayerName()); // Player removed
															// prematurely
			}
			player.setLevel(user.getOldLevel());
			player.setExp(user.getOldExp());
		} catch (Exception e) {
			// Player has left
		}
		this.endTimeMS = System.currentTimeMillis();
		RaceExecutor.finishRace(this, user);
	}

	public User updateUser(Player player) {
		String playerName = player.getName();
		for (User u : getUsers()) {
			if (u.getPlayerName().equals(playerName)) {
				if (!forceRemoveUser(u)) {
					main.logger.info("updateUser() failed to remove user");
				}
				u.setPlayer(player);
				users.add(u);
				return u;
			}
		}
		return null;
	}

	public CheckpointCheck playerAtCheckpoint(Integer[] checks, Player p,
			Server server) {
		int checkpoint = 0;
		Boolean at = false;
		final Map<Integer, SerializableLocation> schecks = this.track
				.getCheckpoints();
		Location pl = p.getLocation();
		for (Integer key : checks) {
			if (schecks.containsKey(key)) {
				try {
					SerializableLocation sloc = schecks.get(key);
					Location check = sloc.getLocation(server);
					double dist = check.distanceSquared(pl); // Squared because
																// of
					// better
					// performance
					p.removeMetadata("checkpoint.distance", main.plugin);
					p.setMetadata("checkpoint.distance", new StatValue(dist,
							main.plugin));
					if (dist < main.plugin.checkpointRadiusSquared) {
						at = true;
						checkpoint = key;
						return new CheckpointCheck(at, checkpoint);
					}
				} catch (Exception e) {
					// Un-measureable distance (Diff. world or sommat)
				}
			}
		}
		return new CheckpointCheck(at, checkpoint);
	}

	public int getMaxCheckpoints() {
		return maxCheckpoints; // Starts at 0
	}

	public Boolean atLine(Server server, Location loc) {
		Location line1 = this.track.getLine1(server);
		Location line2 = this.track.getLine2(server);
		String lineAxis = "x";
		Boolean at = false;
		Boolean l1 = true;
		if (line1.getX() + 0.5 > line2.getX() - 0.5
				&& line1.getX() - 0.5 < line2.getX() + 0.5) {
			lineAxis = "z";
		}
		if (lineAxis == "x") {
			if (line2.getX() < line1.getX()) {
				l1 = false;
			}
			if (l1) {
				if (line2.getX() + 0.5 > loc.getX()
						&& loc.getX() > line1.getX() - 0.5) {
					at = true;
				}
			} else {
				if (line1.getX() + 0.5 > loc.getX()
						&& loc.getX() > line2.getX() - 0.5) {
					at = true;
				}
			}
			if (at) {
				if (line1.getZ() + 4 > loc.getZ()
						&& line1.getZ() - 4 < loc.getZ()) {
					if (line1.getY() + 4 > loc.getY()
							&& line1.getY() - 4 < loc.getY()) {
						return true;
					}
				}
			}
		} else if (lineAxis == "z") {
			if (line2.getZ() < line1.getZ()) {
				l1 = false;
			}
			if (l1) {
				if (line2.getZ() + 0.5 > loc.getZ()
						&& loc.getZ() > line1.getZ() - 0.5) {
					at = true;
				}
			} else {
				if (line1.getZ() + 0.5 > loc.getZ()
						&& loc.getZ() > line2.getZ() - 0.5) {
					at = true;
				}
			}
			if (at) {
				if (line1.getX() + 4 > loc.getX()
						&& line1.getX() - 4 < loc.getX()) {
					if (line1.getY() + 4 > loc.getY()
							&& line1.getY() - 4 < loc.getY()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public void broadcast(String msg) {
		for (User user : getUsersIn()) {
			Player player = null;
			try {
				player = user.getPlayer();
			} catch (PlayerQuitException e) {
				leave(user, true);
			}
			if (player == null) {
				leave(user, true);
			} else {
				player.sendMessage(main.colors.getInfo() + msg);
			}
		}
		return;
	}

	public void clear() {
		users.clear();
		finished.clear();
		this.ended = true;
		this.ending = true;
		return;
	}

	public Boolean removeUser(User user) {
		if (!users.contains(user)) {
			return false;
		}
		users.remove(user);
		user.clear();
		return true;
	}

	public Boolean removeUser(String user) {
		for (User u : getUsers()) {
			if (u.getPlayerName().equals(user)) {
				u.clear();
				users.remove(u);
				return true;
			}
		}
		return false;
	}

	public Boolean forceRemoveUser(User user) {
		if (!removeUser(user)) {
			if (!removeUser(user.getPlayerName())) {
				user.clear();
				return false;
			}
		}
		user.clear();
		return true;
	}

	public Boolean playerUserRegistered(String name) {
		for (User u : getUsers()) {
			if (u.getPlayerName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
