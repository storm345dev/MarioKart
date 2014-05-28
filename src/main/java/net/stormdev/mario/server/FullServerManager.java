package net.stormdev.mario.server;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FullServerManager {
	public static String BUNGEE_LOBBY_ID = "lobby";
	private static FullServerManager instance = null;
	private volatile ServerStage stage = ServerStage.WAITING;
	
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
		//TODO Handle changing MOTD, etc
	}
	
	public FullServerManager(){
		instance = this;
		MarioKart.logger.info("Using full server mode!");
		BUNGEE_LOBBY_ID = MarioKart.config.getString("general.server.bungeelobby");
		MarioKart.logger.info("Using "+BUNGEE_LOBBY_ID+" as the game lobby!");
		Bukkit.getPluginManager().registerEvents(new ServerListener(), MarioKart.plugin);
	}
	
	public void sendToLobby(Player player){
		PlayerServerSender.sendToServer(player, BUNGEE_LOBBY_ID);
	}
}
