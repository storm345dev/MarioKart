package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;

import net.stormdev.mario.utils.RaceQue;
import net.stormdev.mario.utils.RaceTrack;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.TrackCreator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;

public class URaceCommandExecutor implements CommandExecutor {
	main plugin = null;

	public URaceCommandExecutor(main plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("marioRaceAdmin")) {
			if (args.length < 1) {
				return false;
			}
			String command = args[0];
			if (command.equalsIgnoreCase("create")) {
				// /urace create [TrackName]
				if (player == null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.playersOnly"));
					return true;
				}
				if (args.length < 3) {
					return false;
				}
				String trackname = args[1];
				int laps = 3;
				try {
					laps = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				if (laps < 1) {
					laps = 1;
				}
				if (plugin.trackManager.raceTrackExists(trackname)) {
					String msg = main.msgs.get("setup.create.exists");
					msg = msg.replaceAll(Pattern.quote("%name%"), trackname);
					sender.sendMessage(main.colors.getError() + msg);
					return true;
				}
				int id = main.config.getInt("setup.create.wand");
				ItemStack named = new ItemStack(id);
				String start = main.msgs.get("setup.create.start");
				start = start.replaceAll(Pattern.quote("%id%"), "" + id);
				start = start.replaceAll(Pattern.quote("%name%"), named
						.getType().name().toLowerCase());
				sender.sendMessage(main.colors.getInfo() + start);
				RaceTrack track = new RaceTrack(trackname, 2, 2, laps);
				new TrackCreator(player, track); // Create the track
				return true;
			} else if (command.equalsIgnoreCase("delete")) {
				if (args.length < 2) {
					return false;
				}
				String trackname = args[1];
				if (!plugin.trackManager.raceTrackExists(trackname)) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				plugin.trackManager.deleteRaceTrack(trackname);
				String msg = main.msgs.get("general.cmd.delete.success");
				msg = msg.replaceAll("%name%", trackname);
				sender.sendMessage(main.colors.getSuccess() + msg);
				return true;
			} else if (command.equalsIgnoreCase("list")) {
				int page = 1;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						page = 1;
					}
				}
				@SuppressWarnings("unchecked")
				ArrayList<RaceTrack> tracks = (ArrayList<RaceTrack>) plugin.trackManager
						.getRaceTracks().clone();
				ArrayList<String> names = new ArrayList<String>();
				for (RaceTrack track : tracks) {
					names.add(track.getTrackName());
				}
				double total = names.size() / 6;
				int totalpages = (int) Math.ceil(total);
				int pos = (page - 1) * 6;
				if (page > totalpages) {
					page = totalpages;
				}
				if (pos > names.size()) {
					pos = names.size() - 5;
				}
				if (pos < 0) {
					pos = 0;
				}
				if (page < 0) {
					page = 0;
				}
				String msg = main.msgs.get("general.cmd.page");
				msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
				msg = msg.replaceAll(Pattern.quote("%total%"), ""
						+ (totalpages + 1));
				sender.sendMessage(main.colors.getTitle() + msg);
				for (int i = pos; i < (i + 6) && i < names.size(); i++) {
					String Trackname = names.get(i);
					char[] chars = Trackname.toCharArray();
					if (chars.length >= 1) {
						String s = "" + chars[0];
						s = s.toUpperCase();
						Trackname = s + Trackname.substring(1);
					}
					sender.sendMessage(main.colors.getInfo() + Trackname);
				}
				return true;
			} else if (command.equalsIgnoreCase("setLaps")) {
				if (args.length < 3) {
					return false;
				}
				String trackname = args[1];
				if (!plugin.trackManager.raceTrackExists(trackname)) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				String lapsStr = args[2];
				int laps = 3;
				try {
					laps = Integer.parseInt(lapsStr);
				} catch (NumberFormatException e) {
					return false;
				}
				plugin.trackManager.getRaceTrack(trackname).laps = laps;
				plugin.trackManager.save();
				String msg = main.msgs.get("general.cmd.setlaps.success");
				msg = msg.replaceAll(Pattern.quote("%name%"),
						plugin.trackManager.getRaceTrack(trackname)
								.getTrackName());
				sender.sendMessage(main.colors.getSuccess() + msg);
				return true;
			}
			return false;
		} else if (cmd.getName().equalsIgnoreCase("race")) {
			return urace(sender, args, player);
		} else if (cmd.getName().equalsIgnoreCase("racetimes")) {
			if (args.length < 2) {
				return false;
			}
			String trackName = args[0];
			String amount = args[1];
			@SuppressWarnings("unchecked")
			List<String> names = (List<String>) plugin.trackManager
					.getRaceTrackNames().clone();
			for (String n : names) {
				if (n.equalsIgnoreCase(trackName)) {
					trackName = n;
				}
			}
			double d = 5;
			try {
				d = Double.parseDouble(amount);
			} catch (NumberFormatException e) {
				return false;
			}
			SortedMap<String, Double> topTimes = plugin.raceTimes.getTopTimes(
					d, trackName);
			Map<String, Double> times = plugin.raceTimes.getTimes(trackName);
			String msg = main.msgs.get("general.cmd.racetimes");
			msg = msg.replaceAll(Pattern.quote("%n%"), d + "");
			msg = msg.replaceAll(Pattern.quote("%track%"), trackName);
			sender.sendMessage(main.colors.getTitle() + msg);
			Object[] keys = topTimes.keySet().toArray();
			int pos = 1;
			for (Object o : keys) {
				String name = (String) o;
				sender.sendMessage(main.colors.getTitle() + pos + ")"
						+ main.colors.getInfo() + name + "- " + times.get(name)
						+ "s");
				pos++;
			}
			return true;
		}
		return false;
	}

	public Boolean urace(CommandSender sender, String[] args, Player player) {
		if (args.length < 1) {
			return false;
		}
		String command = args[0];
		if (command.equalsIgnoreCase("list")) {
			int page = 1;
			if (args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					page = 1;
				}
			}
			@SuppressWarnings("unchecked")
			ArrayList<RaceTrack> tracks = (ArrayList<RaceTrack>) plugin.trackManager
					.getRaceTracks().clone();
			ArrayList<String> names = new ArrayList<String>();
			for (RaceTrack track : tracks) {
				names.add(track.getTrackName());
			}
			double total = names.size() / 6;
			int totalpages = (int) Math.ceil(total);
			int pos = (page - 1) * 6;
			if (page > totalpages) {
				page = totalpages;
			}
			if (pos > names.size()) {
				pos = names.size() - 5;
			}
			if (pos < 0) {
				pos = 0;
			}
			if (page < 0) {
				page = 0;
			}
			String msg = main.msgs.get("general.cmd.page");
			msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
			msg = msg.replaceAll(Pattern.quote("%total%"), ""
					+ (totalpages + 1));
			sender.sendMessage(main.colors.getTitle() + msg);
			for (int i = pos; i < (i + 6) && i < names.size(); i++) {
				String Trackname = names.get(i);
				char[] chars = Trackname.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					Trackname = s + Trackname.substring(1);
				}
				sender.sendMessage(main.colors.getInfo() + Trackname);
			}
			return true;
		} else if (command.equalsIgnoreCase("join")) {
			if (player == null) {
				sender.sendMessage(main.colors.getError()
						+ main.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			String trackName = null;
			if (args.length < 2) {
				trackName = "auto";
			}
			trackName = args[1];
			RaceType type = RaceType.RACE;
			// /race join test cup
			if (args.length > 2) {
				String t = args[2];
				if (t.equalsIgnoreCase("race")) {
					type = RaceType.RACE;
				} else if (t.equalsIgnoreCase("timed")
						|| t.equalsIgnoreCase("time")
						|| t.equalsIgnoreCase("time_trial")
						|| t.equalsIgnoreCase("time trial")
						|| t.equalsIgnoreCase("time-trial")) {
					type = RaceType.TIME_TRIAL;
				} else if (t.equalsIgnoreCase("cup")
						|| t.equalsIgnoreCase("championship")
						|| t.equalsIgnoreCase("grand")
						|| t.equalsIgnoreCase("grand prix")
						|| t.equalsIgnoreCase("grand-prix")
						|| t.equalsIgnoreCase("grand_prix")) {
					type = RaceType.GRAND_PRIX;
				}
			}
			if (trackName.equalsIgnoreCase("auto")) {
				if (main.plugin.raceMethods.inAGame(player) != null
						|| main.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("race.que.existing"));
					return true;
				}
				List<String> gameArenas = new ArrayList<String>();
				Map<String, Boolean> order = new HashMap<String, Boolean>();
				int waitingPlayers = 0;
				for (String aname : plugin.raceQues.getQues()) {
					RaceQue arena = plugin.raceQues.getQue(aname);
					if (arena.getHowManyPlayers() < arena.getPlayerLimit()
							&& arena.getType() == type) {
						gameArenas.add(aname);
						if (arena.getHowManyPlayers() > waitingPlayers) {
							waitingPlayers = arena.getHowManyPlayers();
						}
					}
				}
				int waitNo = 1;
				List<String> remaining = new ArrayList<String>();
				remaining.addAll(gameArenas);
				for (int i = waitNo; i <= waitingPlayers; i++) {
					for (String aname : gameArenas) {
						RaceQue arena = plugin.raceQues.getQue(aname);
						if (arena.getHowManyPlayers() == waitNo) {
							Boolean reccommendedQueue = true;
							if(arena.getHowManyPlayers() > main.config.getInt("general.race.targetPlayers")){
								reccommendedQueue = false;
							}
							order.put(aname, reccommendedQueue);
							if (remaining.contains(aname)) {
								remaining.remove(aname);
							}
						}
					}
				}
				for (String aname : remaining) {
					order.put(aname, true);
				}
				if (order.size() < 1) {
					// Create a random raceQue
					int min = 0;
					int max = main.plugin.trackManager.getRaceTracks().size() - 1;
					if (main.plugin.trackManager.getRaceTracks().size() < 1) {
						// No tracks created
						sender.sendMessage(main.colors.getError()
								+ main.msgs.get("general.cmd.full"));
						return true;
					}
					int randomNumber;
					try {
						randomNumber = main.plugin.random.nextInt(max - min)
								+ min;
					} catch (Exception e) {
						randomNumber = 0;
					}
					RaceTrack track = main.plugin.trackManager.getRaceTracks()
							.get(randomNumber);
					RaceQue que = new RaceQue(track, type);
					main.plugin.gameScheduler.joinGame(player, track, que, track.getTrackName());
					return true;
				}
				int randomNumber;
				try {
					randomNumber = main.plugin.random.nextInt(order.size());
				} catch (Exception e) {
					randomNumber = 0;
				}
				String name = (String) order.keySet().toArray()[randomNumber];
				RaceQue arena = plugin.raceQues.getQue(name);
				RaceQue other = null;
				Boolean rec = order.get(name);
				while ((arena.getHowManyPlayers() < 1
						|| arena.getType() != type
						|| !rec)
						&& order.size()>0) {
					if(order != null 
							&& !rec
							&& arena.getType() == type){
						//Not reccommended (eg. lots of players) but still valid
						other = arena;
					}
					order.remove(name);
					if(order.size()>1){
					name = (String) order.keySet().toArray()[main.plugin.random.nextInt(order.size())];
					arena = plugin.raceQues.getQue(name);
					rec = order.get(name);
					}
				}
				if(!rec && other != null){
					arena = other;
					name = other.getTrack().getTrackName();
				}
				RaceTrack track = plugin.trackManager.getRaceTrack(name);
				if (track == null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				if (player.getVehicle() != null) {
					Vehicle veh = (Vehicle) player.getVehicle();
					veh.eject();
					veh.remove();
				}
				plugin.gameScheduler.joinGame(player, track, arena, name);
				return true;
			} else {
				RaceTrack track = plugin.trackManager.getRaceTrack(trackName);
				if (track == null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				RaceQue que = new RaceQue(track, type);
				trackName = track.getTrackName();
				if (main.plugin.raceQues.getQue(trackName) != null) {
					que = main.plugin.raceQues.getQue(trackName);
				}
				if (que.getType() != type) {
					if (que.getHowManyPlayers() < 1) {
						plugin.raceQues
								.removeQue(que.getTrack().getTrackName());
						que = new RaceQue(track, type);
					} else {
						// Another queue for different RameType
						String msg = main.msgs.get("race.que.other");
						msg = msg.replaceAll(Pattern.quote("%type%"), que
								.getType().name().toLowerCase());
						sender.sendMessage(main.colors.getError() + msg);
						return true;
					}
				}
				if (main.plugin.raceMethods.inAGame(player) != null
						|| main.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("race.que.existing"));
					return true;
				}
				if (player.getVehicle() != null) {
					Vehicle veh = (Vehicle) player.getVehicle();
					veh.eject();
					veh.remove();
				}
				main.plugin.gameScheduler.joinGame(player, track, que, trackName);
				return true;
			}
		} else if (command.equalsIgnoreCase("queues")
				|| command.equalsIgnoreCase("ques")) {
			int page = 1;
			if (args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					page = 1;
				}
			}
			ArrayList<String> names = new ArrayList<String>();
			names.addAll(plugin.raceQues.getQues());
			double total = names.size() / 6;
			int totalpages = (int) Math.ceil(total);
			int pos = (page - 1) * 6;
			if (page > totalpages) {
				page = totalpages;
			}
			if (pos > names.size()) {
				pos = names.size() - 5;
			}
			if (pos < 0) {
				pos = 0;
			}
			if (page < 0) {
				page = 0;
			}
			String msg = main.msgs.get("general.cmd.page");
			msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
			msg = msg.replaceAll(Pattern.quote("%total%"), ""
					+ (totalpages + 1));
			sender.sendMessage(main.colors.getTitle() + msg);
			for (int i = pos; i < (i + 6) && i < names.size(); i++) {
				String Trackname = names.get(i);
				RaceQue que = plugin.raceQues.getQue(Trackname);
				ChatColor color = ChatColor.GREEN;
				if (que.getHowManyPlayers() > (que.getPlayerLimit() - 1)) {
					color = ChatColor.RED;
				}
				if (que.getHowManyPlayers() > (que.getPlayerLimit() - 2)) {
					color = ChatColor.YELLOW;
				}
				if (que.getHowManyPlayers() < main.config.getInt("race.que.minPlayers")) {
					color = ChatColor.YELLOW;
				}
				char[] chars = Trackname.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					Trackname = color + s + Trackname.substring(1)
							+ main.colors.getInfo() + " (" + color
							+ que.getHowManyPlayers() + main.colors.getInfo()
							+ "/" + que.getPlayerLimit() + ")";
				}
				sender.sendMessage(main.colors.getInfo() + Trackname);
			}
			return true;
		} else if (command.equalsIgnoreCase("leave")) {
			if (player == null) {
				sender.sendMessage(main.colors.getError()
						+ main.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			Boolean game = true;
			Race race = main.plugin.raceMethods.inAGame(player);
			String que = main.plugin.raceMethods.inGameQue(player);
			if (race == null) {
				game = false;
			}
			if (que == null) {
				if (!game) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.leave.fail"));
					return true;
				}
			}
			if (game) {
				race.leave(race.getUser(player.getName()), true);
			} else {
				RaceQue queue = main.plugin.raceQues.getQue(que);
				try {
					main.plugin.gameScheduler.leaveQue(player, queue, queue.getTrack().getTrackName());
				} catch (Exception e) {
					e.printStackTrace();
					//Player not in a queue
					sender.sendMessage(main.colors.getError()
							+ "ERROR occured. Please contact a member of staff.");
					return true;
				}
				String msg = main.msgs.get("general.cmd.leave.success");
				msg = msg.replaceAll(Pattern.quote("%name%"), que);
				sender.sendMessage(main.colors.getSuccess() + msg);
				player.teleport(queue.getTrack().getExit(main.plugin.getServer()));
			}
			return true;
		}
		return false;
	}
}
