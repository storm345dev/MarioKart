package net.stormdev.mario.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.races.MarioKartRaceEndEvent;
import net.stormdev.mario.utils.MetaValue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;

public class ServerListener implements Listener {
	private FullServerManager fsm;
	private final String MOVE_META = "mariokart.moved";
	
	public ServerListener(){
		this.fsm = FullServerManager.get();
		Bukkit.getScheduler().runTaskTimer(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				Player[] online = Bukkit.getOnlinePlayers();
				for(Player player:online){
					if(!player.hasMetadata(MOVE_META)){
						player.setMetadata(MOVE_META, new MetaValue(System.currentTimeMillis(), MarioKart.plugin));
						continue;
					}
					Object o = player.getMetadata(MOVE_META).get(0).value();
					String s = o.toString();
					long moved;
					try {
						moved = Long.parseLong(s);
					} catch (NumberFormatException e) {
						continue;
					}
					long diff = System.currentTimeMillis()-moved;
					if(diff > 50000 && diff < 60000){ //They havent moved for 
						//They are afk!
						player.sendMessage(ChatColor.RED+"WARNING: If you do not move in the next 10 seconds, you'll be afk kicked!");
						continue;
					}
					else if(diff >= 60000){
						player.kickPlayer("Kicked for AFK");
					}
				}
				return;
			}}, 9*20l, 9*20l);
	}
	
	@EventHandler
	void onMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		player.removeMetadata(MOVE_META, MarioKart.plugin);
		player.setMetadata(MOVE_META, new MetaValue(System.currentTimeMillis(), MarioKart.plugin));
	}
	
	
	@EventHandler
	void invClick(InventoryClickEvent event){
		Entity e = event.getWhoClicked();
		if(!(e instanceof Player)){
			return;
		}
		
		if(!fsm.getStage().equals(ServerStage.WAITING)){
			return;
		}
		ItemStack clicked = event.getCurrentItem();
		if(!clicked.isSimilar(FullServerManager.item)
				|| !(clicked.getItemMeta().getDisplayName().equals(FullServerManager.item.getItemMeta().getDisplayName()))){
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	void useLobbyTP(PlayerInteractEvent event){
		Player player = event.getPlayer();
		ItemStack inHand = player.getItemInHand();
		if(!fsm.getStage().equals(ServerStage.WAITING)){
			return;
			
		}
		if(!inHand.isSimilar(FullServerManager.item)
				|| !(inHand.getItemMeta().getDisplayName().equals(FullServerManager.item.getItemMeta().getDisplayName()))){
			return;
		}
		player.teleport(fsm.lobbyLoc); //For when they next login
		player.sendMessage(ChatColor.GRAY+"Teleporting...");
		fsm.sendToLobby(player);
	}
	
	@EventHandler
	void entityDamage(EntityDamageByEntityEvent event){ //Not part of MK
		event.setDamage(0);
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void respawn(PlayerRespawnEvent event){
		if(fsm.getStage().equals(ServerStage.WAITING) || fsm.getStage().equals(ServerStage.STARTING)){
			event.setRespawnLocation(fsm.lobbyLoc);
		}
	}
	
	@EventHandler
	void onPing(ServerListPingEvent event){
		event.setMotd(fsm.getMOTD());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void disconnect(PlayerQuitEvent event){
		event.setQuitMessage(null);
		Player player = event.getPlayer();
		player.removeMetadata(MOVE_META, MarioKart.plugin);
		if(player.getVehicle() != null){
			player.getVehicle().eject();
			player.getVehicle().remove();
		}
		if(fsm != null && fsm.voter != null){
			fsm.voter.removePlayerFromBoard(player);
		}
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	void playerJoin(PlayerJoinEvent event){
		event.setJoinMessage(null);
		final Player player = event.getPlayer();
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.getInventory().clear();
		
		boolean showVoteMsg = true;
		if(!fsm.getStage().getAllowJoin()){
			player.sendMessage(ChatColor.RED+"Unable to join server at this time!");
			Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

				@Override
				public void run() {
					fsm.sendToLobby(player);
					return;
				}}, 5*20l);
			return;
		}
		
		player.sendMessage(ChatColor.BOLD+""+ChatColor.GOLD+"------------------------------");
		player.sendMessage(ChatColor.DARK_RED+"Welcome to MarioKart, "+ChatColor.WHITE+player.getName()+ChatColor.DARK_RED+"!");
		player.sendMessage(ChatColor.BOLD+""+ChatColor.GOLD+"------------------------------");
		
		//Enable resource pack for them:
		String rl = MarioKart.plugin.packUrl;                           //Send them the download url, etc for if they haven't get server RPs enabled
		player.sendMessage(MarioKart.colors.getInfo()
				+ MarioKart.msgs.get("resource.download"));
		String msg = MarioKart.msgs.get("resource.downloadHelp");
		msg = msg.replaceAll(Pattern.quote("%url%"),
				Matcher.quoteReplacement(ChatColor.RESET + ""));
		player.sendMessage(MarioKart.colors.getInfo() + msg);
		player.sendMessage(rl); //new line
		
		if(!MarioKart.plugin.resourcedPlayers.contains(player.getName()) //Send them the RP for if they have got server RPs enabled
				&& MarioKart.plugin.fullPackUrl != null
				&& MarioKart.plugin.fullPackUrl.length() > 0){
			Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

				@Override
				public void run() {
					player.setResourcePack(MarioKart.plugin.fullPackUrl);
					MarioKart.plugin.resourcedPlayers.add(player.getName());
					return;
				}}, 20l);
		}
		
		final Location spawnLoc = fsm.lobbyLoc;
		if(player.getVehicle() != null){
			player.getVehicle().eject();
			player.getVehicle().remove();
			Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

				@Override
				public void run() {
					player.teleport(spawnLoc);
					return;
				}}, 2l);
		}
		else {
			player.teleport(spawnLoc);
		}
		
		if(fsm.getStage().equals(ServerStage.WAITING)){
			player.getInventory().addItem(FullServerManager.item.clone());
			if(fsm.voter == null){
				showVoteMsg = false;
				fsm.changeServerStage(ServerStage.WAITING);
			}
			fsm.voter.addPlayerToBoard(player);
			if(showVoteMsg){
				Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

					@Override
					public void run() {
						player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
						if(fsm.voter != null){
							player.sendMessage(fsm.voter.getHelpString());
							player.sendMessage(fsm.voter.getAvailTracksString());
						}
						player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
						return;
					}}, 2l);
			}
		}
		else if(fsm.getStage().equals(ServerStage.STARTING)){
			player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
			player.sendMessage(ChatColor.GOLD+"Game starting in under 10 seconds...");
			player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		}
		player.setGameMode(GameMode.SURVIVAL);
	}
	
	@EventHandler
	public void raceEnding(MarioKartRaceEndEvent event){
		//Reset game
		fsm.changeServerStage(ServerStage.RESTARTING);
		//wait...
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				Player[] online = Bukkit.getOnlinePlayers();
				for(Player p:online){
					fsm.sendToLobby(p);
				}
				Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

					@Override
					public void run() {
						fsm.changeServerStage(ServerStage.WAITING);
						return;
					}}, 10*20l);
				return;
			}}, 10*20l);
	}
	
	@EventHandler
	void foodChange(FoodLevelChangeEvent event){
		Entity e = event.getEntity();
		if(!(e instanceof Player)){
			return;
		}
		event.setFoodLevel(20);
		event.setCancelled(true);
	}
	
	@EventHandler
	void itemDrop(PlayerDropItemEvent event){
		event.setCancelled(true);
	}
}
