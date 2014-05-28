package net.stormdev.mario.server;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.tracks.RaceTrack;
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
	
	private RaceType mode;
	private RaceTrack track;
	private volatile Race race;
	private boolean starting = false;
	
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
		case RESTARTING: {
			voter = null;
			track = null;
			race = null;
			starting = false;
			mode = RaceType.RACE;
		}
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
		try {
			Bukkit.getMessenger().registerOutgoingPluginChannel(MarioKart.plugin, "BungeeCord");
		} catch (Exception e) {
			//OH DEAR
			throw new RuntimeException("BungeeCord unable to work");
		}
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
	
	public void trackSelected(final String trackName){
		if(starting){
			return;
		}
		starting = true;
		changeServerStage(ServerStage.STARTING);
		voter = null; //Stop voting stuff working
		
		int online = Bukkit.getOnlinePlayers().length;
		if(online < 2){
			mode = RaceType.TIME_TRIAL;
		}
		else {
			mode = RaceType.RACE;
		}
		
		track = MarioKart.plugin.trackManager.getRaceTrack(trackName);
		if(track == null){
			changeServerStage(ServerStage.WAITING);
			return;
		}
		
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"Map: "+ChatColor.GOLD+trackName);
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"Mode: "+ChatColor.GOLD+mode.name().toLowerCase());
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"Please wait "+ChatColor.GOLD+"10s"+ChatColor.DARK_RED+" for the game to start!");
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				//Start the game!
				changeServerStage(ServerStage.PLAYING);
				
				Player[] players = Bukkit.getOnlinePlayers();
				if(players.length < 1){
					changeServerStage(ServerStage.WAITING); //Reset the server, nobody is on anymore
					return;
				}
				RaceQueue queue = new RaceQueue(track, mode, players[0]);
				for(int i=1;i<players.length;i++){
					Player p = players[i];
					if(p == null || !p.isOnline()){
						continue;
					}
					if(p.hasPermission("mariokart.premium")){
						if(queue.playerCount() < queue.playerLimit()){
							queue.addPlayer(p);
						}
						else {
							p.sendMessage(ChatColor.RED+"Sorry, there are not enough slots for you to join in with this race :(");
							sendToLobby(p);
						}
					}
				}
				for(int i=1;i<players.length;i++){
					Player p = players[i];
					if(p == null || !p.isOnline()){
						continue;
					}
					if(!p.hasPermission("mariokart.premium") && !queue.containsPlayer(p)){
						if(queue.playerCount() < queue.playerLimit()){
							queue.addPlayer(p);
						}
						else {
							p.sendMessage(ChatColor.RED+"Sorry, there are not enough slots for you to join in with this race :(");
							sendToLobby(p);
						}
					}
				}
				race = new Race(track,
						track.getTrackName(), mode);
				List<Player> q = new ArrayList<Player>(queue.getPlayers());
				for (Player p : q) {
					if (p != null && p.isOnline()) {
						if(race.getUsers().size() < race.getTrack().getMaxPlayers()){
							race.join(p);
						}
						else {
							p.sendMessage(ChatColor.RED+"Sorry, there are not enough slots for you to join in with this race :(");
							sendToLobby(p);
						}
						queue.removePlayer(p);
					}
				}
				if (race.getUsers().size() > 0) {
					MarioKart.plugin.raceScheduler.startRace(race.getTrackName(), race);
				}
				return;
			}}, 10*20l);
	}
}
