package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.shop.Shop;
import net.stormdev.mario.tracks.RaceTrack;
import net.stormdev.mario.tracks.TrackCreator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class URaceCommandExecutor implements CommandExecutor {
	main plugin = null;

	public URaceCommandExecutor(main plugin) {
		this.plugin = plugin;
	}

	@Override
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
				if(pos <= d){
					String name = (String) o;
					sender.sendMessage(main.colors.getTitle() + pos + ")"
							+ main.colors.getInfo() + name + "- " + times.get(name)
							+ "s");
					pos++;
				}
			}
			return true;
		}
		return false;
	}

	public Boolean urace(CommandSender sender, String[] args, final Player player) {
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
				} else if(t.equalsIgnoreCase("auto")
						|| t.equalsIgnoreCase("random")){
					type = RaceType.AUTO;
				}
			}
			if (player.getVehicle() != null) {
				sender.sendMessage(main.colors.getError()
						+ "Cannot execute whilst in a vehicle");
				return true;
			}
			if (trackName.equalsIgnoreCase("auto")) {
				if (main.plugin.raceMethods.inAGame(player, false) != null
						|| main.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("race.que.existing"));
					return true;
				}
				plugin.raceScheduler.joinAutoQueue(player, type);
				return true;
			} else {
				if (main.plugin.raceMethods.inAGame(player, false) != null
						|| main.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("race.que.existing"));
					return true;
				}
				RaceTrack track = plugin.trackManager.getRaceTrack(trackName);
				if (track == null) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				main.plugin.raceScheduler.joinQueue(player, track, type);
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
			Map<UUID, RaceQueue> queues = plugin.raceQueues.getAllQueues();
			double total = queues.size() / 6;
			int totalpages = (int) Math.ceil(total);
			int pos = (page - 1) * 6;
			if (page > totalpages) {
				page = totalpages;
			}
			if (pos > queues.size()) {
				pos = queues.size() - 5;
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
			ArrayList<UUID> keys = new ArrayList<UUID>(queues.keySet());
			for (int i = pos; i < (i + 6) && i < queues.size(); i++) {
				UUID id = keys.get(i);
				RaceQueue queue = queues.get(id);
				String trackName = queue.getTrackName();
				ChatColor color = ChatColor.GREEN;
				int playerCount = queue.playerCount();
				if (playerCount > (queue.playerLimit() - 1)) {
					color = ChatColor.RED;
				}
				if (playerCount > (queue.playerLimit() - 2)) {
					color = ChatColor.YELLOW;
				}
				if (playerCount < main.config.getInt("race.que.minPlayers")) {
					color = ChatColor.YELLOW;
				}
				char[] chars = trackName.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					trackName = color + s + trackName.substring(1)
							+ main.colors.getInfo() + " (" + color
							+ queue.playerCount() + main.colors.getInfo() + "/"
							+ queue.playerLimit() + ")" + " ["
							+ queue.getRaceMode().name().toLowerCase() + "]";
				}
				sender.sendMessage(main.colors.getInfo() + trackName);
			}
			return true;
		} else if (command.equalsIgnoreCase("leave")) {
			if (player == null) {
				sender.sendMessage(main.colors.getError()
						+ main.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			main.plugin.hotBarManager.clearHotBar(player.getName());
			Boolean game = true;
			Race race = main.plugin.raceMethods.inAGame(player, false);
			RaceQueue queue = main.plugin.raceMethods.inGameQue(player);
			if (race == null) {
				game = false;
			}
			if (queue == null) {
				if (!game) {
					sender.sendMessage(main.colors.getError()
							+ main.msgs.get("general.cmd.leave.fail"));
					return true;
				}
			}
			if (game) {
				User u = race.getUser(player.getName());
				race.leave(u, true);
				u.clear();
			} else {
				final RaceTrack track = queue.getTrack();
				try {
					main.plugin.raceScheduler.leaveQueue(player, queue);
				} catch (Exception e) {
					e.printStackTrace();
					// Player not in a queue
					sender.sendMessage(main.colors.getError()
							+ "ERROR occured. Please contact a member of staff.");
					return true;
				}
				String msg = main.msgs.get("general.cmd.leave.success");
				msg = msg.replaceAll(Pattern.quote("%name%"),
						queue.getTrackName());
				sender.sendMessage(main.colors.getSuccess() + msg);
				player.teleport(track.getExit(main.plugin.getServer()));
				player.setBedSpawnLocation(
						track.getExit(main.plugin.getServer()), true);
			}
			return true;
		} else if (command.equalsIgnoreCase("shop")) {
			if (player == null) {
				sender.sendMessage(main.colors.getError()
						+ main.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			if(!main.config.getBoolean("general.upgrades.enable")){
				sender.sendMessage(main.colors.getError()+main.msgs.get("general.disabled"));
				return true;
			}
			Shop.openShop(player);
			return true;
		}
		return false;
	}
}
