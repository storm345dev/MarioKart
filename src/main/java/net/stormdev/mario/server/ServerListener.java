package net.stormdev.mario.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener implements Listener {
	private FullServerManager fsm;
	public ServerListener(){
		this.fsm = FullServerManager.get();
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
		event.setMotd("Stage: "+fsm.getMOTD());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void disconnect(PlayerQuitEvent event){
		event.setQuitMessage(null);
		Player player = event.getPlayer();
		if(player.getVehicle() != null){
			player.getVehicle().eject();
			player.getVehicle().remove();
		}
		if(fsm.voter != null){
			fsm.voter.removePlayerFromBoard(player);
		}
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	void playerJoin(PlayerJoinEvent event){
		event.setJoinMessage(null);
		final Player player = event.getPlayer();
		if(!fsm.getStage().getAllowJoin()){
			player.kickPlayer("Unable to join server at this time!");
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
			player.setTexturePack(MarioKart.plugin.fullPackUrl);
			MarioKart.plugin.resourcedPlayers.add(player.getName());
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
			fsm.voter.addPlayerToBoard(player);
			Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

				@Override
				public void run() {
					player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
					player.sendMessage(fsm.voter.getHelpString());
					player.sendMessage(fsm.voter.getAvailTracksString());
					player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
					return;
				}}, 2l);
		}
	}
}
