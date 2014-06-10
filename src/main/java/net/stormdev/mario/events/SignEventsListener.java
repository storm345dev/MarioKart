package net.stormdev.mario.events;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SignEventsListener implements Listener {
	private MarioKart plugin;
	
	public SignEventsListener(MarioKart plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	void signClicker(final PlayerInteractEvent event) { //Handle people clicking on signs
		MarioKart.powerupManager.calculate(event.getPlayer(), event);
		if(MarioKart.fullServer){
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (!(event.getClickedBlock().getState() instanceof Sign)) {
			return;
		}
		final Sign sign = (Sign) event.getClickedBlock().getState();
		String[] lines = sign.getLines();
		MarioKart.plugin.getServer().getScheduler().runTaskAsynchronously(MarioKart.plugin, new BukkitRunnable(){

			@Override
			public void run() {
				if(plugin.signManager.isQueueSign(sign)){
					String trackName = ChatColor.stripColor(sign.getLine(0));
					MarioKart.plugin.raceCommandExecutor.urace(event.getPlayer(), new String[] {
						"join", trackName, "auto" },
						event.getPlayer());
				}
				return;
			}});
		if (!ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")) {
			return;
		}
		String cmd = ChatColor.stripColor(lines[1]);
		if (cmd.equalsIgnoreCase("list")) {
			int page = 1;
			try {
				page = Integer.parseInt(ChatColor.stripColor(lines[2]));
			} catch (NumberFormatException e) {
			}
			MarioKart.plugin.raceCommandExecutor.urace(event.getPlayer(), new String[] { "list",
					"" + page }, event.getPlayer());
		} else if (cmd.equalsIgnoreCase("leave")
				|| cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) {
			MarioKart.plugin.raceCommandExecutor.urace(event.getPlayer(), new String[] { "leave" },
					event.getPlayer());
		} else if (cmd.equalsIgnoreCase("join")) {
			String mode = ChatColor.stripColor(lines[3]);
			if (mode.length() > 0) {
				MarioKart.plugin.raceCommandExecutor.urace(event.getPlayer(), new String[] {
						"join", ChatColor.stripColor(lines[2]).toLowerCase(),
						mode }, event.getPlayer());
			} else {
				MarioKart.plugin.raceCommandExecutor.urace(event.getPlayer(), new String[] {
						"join", ChatColor.stripColor(lines[2]).toLowerCase() },
						event.getPlayer());
			}
		} else if (cmd.equalsIgnoreCase("shop")) {
			MarioKart.plugin.raceCommandExecutor.urace(event.getPlayer(), new String[] { "shop" },
					event.getPlayer());
		}
		return;
	}

	@EventHandler
	void signWriter(SignChangeEvent event) { //Handle people making signs
		String[] lines = event.getLines();
		if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")) {
			lines[0] = MarioKart.colors.getTitle() + "[MarioKart]";
			Boolean text = true;
			String cmd = ChatColor.stripColor(lines[1]);
			event.getPlayer().sendMessage("Is item sign: "+(cmd.trim().equalsIgnoreCase("items")));
			if (cmd.equalsIgnoreCase("list")) {
				lines[1] = MarioKart.colors.getInfo() + "List";
				if (!(lines[2].length() < 1)) {
					text = false;
				}
				lines[2] = MarioKart.colors.getSuccess()
						+ ChatColor.stripColor(lines[2]);
			} else if (cmd.equalsIgnoreCase("join")) {
				lines[1] = MarioKart.colors.getInfo() + "Join";
				lines[2] = MarioKart.colors.getSuccess()
						+ ChatColor.stripColor(lines[2]);
				if (lines[2].equalsIgnoreCase("auto")) {
					lines[2] = MarioKart.colors.getTp() + "Auto";
				}
				lines[3] = MarioKart.colors.getInfo() + lines[3];
				text = false;
			} else if (cmd.equalsIgnoreCase("shop")) {
				lines[1] = MarioKart.colors.getInfo() + "Shop";

			} else if (cmd.equalsIgnoreCase("leave")
					|| cmd.equalsIgnoreCase("exit")
					|| cmd.equalsIgnoreCase("quit")) {
				char[] raw = cmd.toCharArray();
				if (raw.length > 1) {
					String start = "" + raw[0];
					start = start.toUpperCase();
					String body = "";
					for (int i = 1; i < raw.length; i++) {
						body = body + raw[i];
					}
					body = body.toLowerCase();
					cmd = start + body;
				}
				lines[1] = MarioKart.colors.getInfo() + cmd;
			} else if (cmd.trim().equalsIgnoreCase("items")) {
				/*
				Location above = event.getBlock().getLocation().add(0, 1.4, 0);
				EnderCrystal crystal = (EnderCrystal) above.getWorld()
						.spawnEntity(above, EntityType.ENDER_CRYSTAL);
				above.getBlock().setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.WEST)
						.setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.NORTH)
						.setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.NORTH_WEST)
						.setType(Material.COAL_BLOCK);
				crystal.setFireTicks(0);
				crystal.setMetadata("race.pickup", new StatValue(true, plugin));
				
				*/
				text = false;
				event.getPlayer().sendMessage("Creating item box...");
				MarioKart.powerupManager.spawnItemPickupBox(event.getBlock().getLocation());
			} else if(cmd.equalsIgnoreCase("queues")){ 
				String track = ChatColor.stripColor(lines[2]);
				if(track.length() < 1){
					return; //No track
				}
				track = plugin.signManager.getCorrectName(track);
				if(!plugin.trackManager.raceTrackExists(track)){
					event.getPlayer().sendMessage(MarioKart.colors.getSuccess()+MarioKart.msgs.get("setup.fail.queueSign"));
					return;
				}
				//Register sign
				plugin.signManager.addQueueSign(track, event.getBlock().getLocation());
				//Tell the player it was registered successfully
				event.getPlayer().sendMessage(MarioKart.colors.getSuccess()+MarioKart.msgs.get("setup.create.queueSign"));
				final String t = track;
				MarioKart.plugin.getServer().getScheduler().runTaskLater(plugin, new BukkitRunnable(){

					@Override
					public void run() {
						plugin.signManager.updateSigns(t);
						return;
					}}, 2l);
				
				text = false;
			} else {
				text = false;
			}
			if (text) {
				lines[2] = ChatColor.ITALIC + "Right click";
				lines[3] = ChatColor.ITALIC + "to use";
			}
		}
	}
}
