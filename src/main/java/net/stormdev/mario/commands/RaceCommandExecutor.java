package net.stormdev.mario.commands;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.User;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.server.FullServerManager;
import net.stormdev.mario.server.SpectatorMode;
import net.stormdev.mario.shop.Shop;
import net.stormdev.mario.tracks.RaceTrack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaceCommandExecutor implements CommandExecutor {
	private MarioKart plugin;
	public RaceCommandExecutor(MarioKart plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("race")) {
			return urace(sender, args, player);
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
			String msg = MarioKart.msgs.get("general.cmd.page");
			msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
			msg = msg.replaceAll(Pattern.quote("%total%"), ""
					+ (totalpages + 1));
			sender.sendMessage(MarioKart.colors.getTitle() + msg);
			for (int i = pos; i < (i + 6) && i < names.size(); i++) {
				String Trackname = names.get(i);
				char[] chars = Trackname.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					Trackname = s + Trackname.substring(1);
				}
				sender.sendMessage(MarioKart.colors.getInfo() + Trackname);
			}
			return true;
		}
		else if(command.equalsIgnoreCase("quit")){ //Quit to lobby
			Bukkit.getScheduler().runTask(MarioKart.plugin, new Runnable(){

				@Override
				public void run() {
					player.getInventory().clear();
					if(FullServerManager.get().spectators != null && FullServerManager.get().spectators.isSpectating(player)){
						FullServerManager.get().spectators.stopSpectating(player);
					}
					player.teleport(FullServerManager.get().lobbyLoc); //For when they next login
					player.sendMessage(ChatColor.GRAY+"Teleporting...");
					FullServerManager.get().sendToLobby(player);
					return;
				}});
			return true;
		}
		else if (command.equalsIgnoreCase("join")) {
			if(MarioKart.fullServer){
				return true;
			}
			if (player == null) {
				sender.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("general.cmd.playersOnly"));
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
				sender.sendMessage(MarioKart.colors.getError()
						+ "Cannot execute whilst in a vehicle");
				return true;
			}
			if (trackName.equalsIgnoreCase("auto")) {
				if (MarioKart.plugin.raceMethods.inAGame(player, false) != null
						|| MarioKart.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("race.que.existing"));
					return true;
				}
				plugin.raceScheduler.joinAutoQueue(player, type);
				return true;
			} else {
				if (MarioKart.plugin.raceMethods.inAGame(player, false) != null
						|| MarioKart.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("race.que.existing"));
					return true;
				}
				RaceTrack track = plugin.trackManager.getRaceTrack(trackName);
				if (track == null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				MarioKart.plugin.raceScheduler.joinQueue(player, track, type);
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
			String msg = MarioKart.msgs.get("general.cmd.page");
			msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
			msg = msg.replaceAll(Pattern.quote("%total%"), ""
					+ (totalpages + 1));
			sender.sendMessage(MarioKart.colors.getTitle() + msg);
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
				if (playerCount < queue.getTrack().getMinPlayers()) {
					color = ChatColor.YELLOW;
				}
				char[] chars = trackName.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					trackName = color + s + trackName.substring(1)
							+ MarioKart.colors.getInfo() + " (" + color
							+ queue.playerCount() + MarioKart.colors.getInfo() + "/"
							+ queue.playerLimit() + ")" + " ["
							+ queue.getRaceMode().name().toLowerCase() + "]";
				}
				sender.sendMessage(MarioKart.colors.getInfo() + trackName);
			}
			return true;
		} else if (command.equalsIgnoreCase("leave")) {
			if (player == null) {
				sender.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			MarioKart.plugin.hotBarManager.clearHotBar(player.getName());
			Boolean game = true;
			Race race = MarioKart.plugin.raceMethods.inAGame(player, false);
			RaceQueue queue = MarioKart.plugin.raceMethods.inGameQue(player);
			if (race == null) {
				game = false;
			}
			if (queue == null) {
				if (!game) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.leave.fail"));
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
					MarioKart.plugin.raceScheduler.leaveQueue(player, queue);
				} catch (Exception e) {
					e.printStackTrace();
					// Player not in a queue
					sender.sendMessage(MarioKart.colors.getError()
							+ "ERROR occured. Please contact a member of staff.");
					return true;
				}
				String msg = MarioKart.msgs.get("general.cmd.leave.success");
				msg = msg.replaceAll(Pattern.quote("%name%"),
						queue.getTrackName());
				sender.sendMessage(MarioKart.colors.getSuccess() + msg);
				player.teleport(track.getExit(MarioKart.plugin.getServer()));
				player.setBedSpawnLocation(
						track.getExit(MarioKart.plugin.getServer()), true);
			}
			return true;
		} else if (command.equalsIgnoreCase("shop")) {
			if (player == null) {
				sender.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			if(!MarioKart.config.getBoolean("general.upgrades.enable")){
				sender.sendMessage(MarioKart.colors.getError()+MarioKart.msgs.get("general.disabled"));
				return true;
			}
			Shop.openShop(player);
			return true;
		}
		return false;
	}
}
