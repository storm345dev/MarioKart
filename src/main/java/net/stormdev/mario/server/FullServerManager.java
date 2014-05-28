package net.stormdev.mario.server;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.utils.LocationStrings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FullServerManager {
	public static String BUNGEE_LOBBY_ID = "lobby";
	private static FullServerManager instance = null;
	private volatile ServerStage stage = ServerStage.WAITING;
	public VoteHandler voter = null;
	public Location lobbyLoc;
	
	public static FullServerManager get(){
		return instance;
	}
	
	public ServerStage getStage(){
		return stage;
	}
	
	public String getMOTD(){
		return stage.getMOTD();
	}
	
	public void changeServerStage(ServerStage now){
		stage = now;
		
		switch(stage){
		case PLAYING:
			break;
		case RESTARTING:
			break;
		case STARTING:
			break;
		case WAITING: {
			voter = new VoteHandler();
		}
			break;
		default:
			break;
		}
	}
	
	public FullServerManager(){
		instance = this;
		MarioKart.logger.info("Using full server mode!");
		BUNGEE_LOBBY_ID = MarioKart.config.getString("general.server.bungeelobby");
		MarioKart.logger.info("Using "+BUNGEE_LOBBY_ID+" as the game lobby!");
		Bukkit.getPluginManager().registerEvents(new ServerListener(), MarioKart.plugin);
		lobbyLoc = LocationStrings.getLocation(MarioKart.config.getString("general.server.gamelobby"));
		changeServerStage(ServerStage.WAITING);
	}
	
	public void sendToLobby(Player player){
		PlayerServerSender.sendToServer(player, BUNGEE_LOBBY_ID);
	}
	
	public void trackSelected(String trackName){
		//TODO
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		//TODO Announce track
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
	}
}
