package net.stormdev.mario.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.tracks.RaceTrack;
import net.stormdev.mario.utils.LocationStrings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FullServerManager {
	public static String BUNGEE_LOBBY_ID = "lobby";
	private static FullServerManager instance = null;
	private volatile ServerStage stage = ServerStage.WAITING;
	public VoteHandler voter = null;
	public Location lobbyLoc;
	
	private RaceType mode;
	private RaceTrack track;
	private volatile Race race;
	private SpectatorMode spectators = null;
	private boolean starting = false;
	
	public static final ItemStack item;
	
	static {
		item = new ItemStack(Material.BOOK);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(ChatColor.DARK_RED+"Exit to lobby");
		im.setLore(Arrays.asList(new String[]{ChatColor.GRAY+"Right click to use"}));
		
		item.setItemMeta(im);
	}
	
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
		case PLAYING: {
			voter = null;
		}
			break;
		case RESTARTING: {
			voter = null;
			track = null;
			race = null;
			starting = false;
			mode = RaceType.RACE;
			spectators.endSpectating();
		}
			break;
		case STARTING: {
			voter = null;
			Player[] online = Bukkit.getOnlinePlayers();
			for(Player player:online){
				player.getInventory().clear();
			}
		}
			break;
		case WAITING: {
			if(voter == null){
				voter = new VoteHandler();
			}
			spectators.endSpectating();
			Player[] online = Bukkit.getOnlinePlayers();
			for(Player player:online){
				player.getInventory().clear();
				player.getInventory().addItem(item.clone());
				player.teleport(lobbyLoc);
				if(spectators.isSpectating(player)){
					spectators.stopSpectating(player);
				}
			}
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
		spectators = new SpectatorMode();
		Bukkit.getScheduler().runTaskTimerAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				if(stage.equals(ServerStage.PLAYING) && MarioKart.plugin.raceScheduler.getRacesRunning() < 1){
					changeServerStage(ServerStage.RESTARTING);
					Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

						@Override
						public void run() {
							Player[] online = Bukkit.getOnlinePlayers();
							for(Player p:online){
								sendToLobby(p);
							}
							Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

								@Override
								public void run() {
									changeServerStage(ServerStage.WAITING);
									return;
								}}, 10*20l);
							return;
						}}, 10*20l);
				}
				return;
			}}, 30*20l, 30*20l);
		changeServerStage(ServerStage.WAITING);
	}
	
	public void sendToLobby(Player player){
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.getInventory().clear();
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
				if(players.length < 1 || track == null || track.getTrackName() == null || mode == null){
					changeServerStage(ServerStage.WAITING); //Reset the server, nobody is on anymore
					return;
				}
				race = new Race(track,
						track.getTrackName(), mode);
				List<Player> q = new ArrayList<Player>(Arrays.asList(Bukkit.getOnlinePlayers()));
				for (Player p : q) {
					if (p != null && p.isOnline()) {
						if(race.getUsers().size() < race.getTrack().getMaxPlayers()){
							race.join(p);
						}
						else {
							p.sendMessage(ChatColor.RED+"Sorry, there are not enough slots for you to join in with this race :(");
							sendToLobby(p);
						}
					}
				}
				if (race.getUsers().size() > 0) {
					MarioKart.plugin.raceScheduler.startRace(race.getTrackName(), race);
				}
				return;
			}}, 10*20l);
	}
	
	public void onEnd(final Player player, final boolean quit){
		if(quit){
			Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

				@Override
				public void run() {
					FullServerManager.get().sendToLobby(player);
					return;
				}}, 5*20l);
			return;
		}
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				spectators.add(player);
				return;
			}}, 5l);
		
	}
}
