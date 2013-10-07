package net.stormdev.ucars.race;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import net.stormdev.ucars.utils.CheckpointCheck;
import net.stormdev.ucars.utils.DoubleValueComparator;
import net.stormdev.ucars.utils.RaceEndEvent;
import net.stormdev.ucars.utils.RaceFinishEvent;
import net.stormdev.ucars.utils.RaceStartEvent;
import net.stormdev.ucars.utils.RaceTrack;
import net.stormdev.ucars.utils.RaceUpdateEvent;
import net.stormdev.ucars.utils.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.useful.ucarsCommon.StatValue;

public class Race {
	public List<String> players = new ArrayList<String>();
	public List<String> inplayers = new ArrayList<String>();
	private String gameId = "";
	private RaceTrack track = null;
	private String trackName = "";
	private String winner = null;
	public String winning = "";
	public Boolean running = false;
	public long tickrate = 6;
	public long scorerate = 15;
	private BukkitTask task = null;
	private BukkitTask scoreCalcs = null;
	public int maxCheckpoints = 3;
	public int totalLaps = 3;
	public ArrayList<String> finished = new ArrayList<String>();
	public int finishCountdown = 60;
	Boolean ending = false;
	Boolean ended = false;
	public Scoreboard board = null;
	public Objective scores = null;
	public Objective scoresBoard = null;
	public Map<String, Integer> checkpoints = new HashMap<String, Integer>();
	public Map<String, Integer> lapsLeft = new HashMap<String, Integer>();
	public Map<String, ItemStack[]> oldInventories = new HashMap<String, ItemStack[]>();
	public Race(RaceTrack track, String trackName){
		this.gameId = UUID.randomUUID().toString();
		this.track = track;
		this.trackName = trackName;
		this.totalLaps = this.track.getLaps();
		this.maxCheckpoints = this.track.getCheckpoints().size()-1;
		this.tickrate = main.config.getLong("general.raceTickrate");
		this.scorerate = (long) ((this.tickrate*2)+(this.tickrate/0.5));
		this.board = main.plugin.getServer().getScoreboardManager().getNewScoreboard();
		this.scores = board.registerNewObjective("", "dummy");
	    scores.setDisplaySlot(DisplaySlot.BELOW_NAME);
	    this.scoresBoard = board.registerNewObjective(ChatColor.GOLD+"Race Positions", "dummy");
	    scoresBoard.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	public void setOldInventories(Map<String, ItemStack[]> inventories){
		this.oldInventories = inventories;
		return;
	}
	public Map<String, ItemStack[]> getOldInventories(){
		return this.oldInventories;
	}
    public List<String> getInPlayers(){
    	return this.inplayers;
    }
    public void setInPlayers(List<String> in){
    	this.inplayers = in;
    	return;
    }
    public void playerOut(String name){
    	if(this.inplayers.contains(name)){
    	this.inplayers.remove(name);
    	}
    }
	public Boolean join(String playername){
		if(players.size() < this.track.getMaxPlayers()){
			players.add(playername);
			return true;
		}
		return false;
	}
	public void leave(String playername, Boolean quit){
		if(quit){
		this.getPlayers().remove(playername);
		if(this.getPlayers().size() < 2){
		ArrayList<String> plz = new ArrayList<String>();
		plz.addAll(getInPlayers());
		for(String pname:plz){
			if(!(main.plugin.getServer().getPlayer(pname) == null) && !playername.equals(pname)){
				String msg = main.msgs.get("race.end.soon");
				main.plugin.getServer().getPlayer(pname).sendMessage(main.colors.getInfo()+msg);
			}
		}
		startEndCount();
		}
		}
		this.playerOut(playername);
		Player player = main.plugin.getServer().getPlayer(playername);
		player.removeMetadata("car.stayIn", main.plugin);
		if(quit){
		this.checkpoints.remove(playername);
		this.lapsLeft.remove(playername);
    	this.scoresBoard.getScore(main.plugin.getServer().getOfflinePlayer(playername)).setScore(0);
    	this.board.resetScores(main.plugin.getServer().getOfflinePlayer(playername));
		if(player != null){
			player.getInventory().clear();
			if(player.getVehicle()!=null){
				Vehicle veh = (Vehicle) player.getVehicle();
				veh.eject();
				veh.remove();
			}
		}
		if(this.getOldInventories().containsKey(playername)){
			if(player != null){
    				player.removeMetadata("car.stayIn", main.plugin);
		player.getInventory().setContents(this.getOldInventories().get(playername));
			}
		this.getOldInventories().remove(playername);
		}
		if(player != null){
			player.setGameMode(GameMode.SURVIVAL);
			player.teleport(player.getWorld().getSpawnLocation());
			player.sendMessage(ChatColor.GOLD+"Successfully quit the race!");
		    player.setScoreboard(main.plugin.getServer().getScoreboardManager().getMainScoreboard());	
		}
		for(String playerName:this.getPlayers()){
			if(main.plugin.getServer().getPlayer(playerName) != null && main.plugin.getServer().getPlayer(playerName).isOnline()){
				Player p=main.plugin.getServer().getPlayer(playerName);
				p.sendMessage(ChatColor.GOLD+playername+" quit the race!");
			}
		}
		}
		try {
			recalculateGame();
		} catch (Exception e) {
		}
		return;
	}
	public void recalculateGame(){
		if(this.inplayers.size() < 1){
			this.running = false;
			this.ended = true;
			this.ending = true;
			try {
				end();
			} catch (Exception e) {
			}
			main.plugin.gameScheduler.reCalculateQues();
		}
	}
	public Boolean isEmpty(){
		if(this.players.size() < 1){
			return true;
		}
		return false;
	}
	public String getGameId(){
		return this.gameId;
	}
	public String getTrackName(){
		return this.trackName;
	}
	public RaceTrack getTrack(){
		return this.track;
	}
    public List<String> getPlayers(){
    	return this.players;
    }
    public void setWinner(String winner){
    	this.winner = winner;
    	return;
    }
    public void startEndCount(){
    	final int count = this.finishCountdown;
    	final Race race = this;
    	main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			public void run() {
				int z = count;
				while(z>0){
					z--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				if(!ended){
				try {
					try {
						main.plugin.getServer().getScheduler().runTask(main.plugin, new Runnable(){

							public void run() {
								race.end();
								return;
							}});
					} catch (Exception e) {
						race.end();
					}
				} catch (IllegalArgumentException e) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					run();
					return;
				}
				}
				return;
			}});
    }
    public String getWinner(){
    	return this.winner;
    }
    public Boolean getRunning(){
    	return this.running;
    }
    public void start(){
    	this.running = true;
    	final Race game = this;
    	for(String pname:this.getInPlayers()){
    		main.plugin.getServer().getPlayer(pname).setScoreboard(board);
    	}
    	this.task = main.plugin.getServer().getScheduler().runTaskTimer(main.plugin, new Runnable(){

			public void run() {
				RaceUpdateEvent event = new RaceUpdateEvent(game);
				main.plugin.getServer().getPluginManager().callEvent(event);
				return;
			}}, tickrate, tickrate);
    	this.scoreCalcs = main.plugin.getServer().getScheduler().runTaskTimer(main.plugin, new Runnable(){

			public void run() {
		    	SortedMap<String, Double> sorted = game.getRaceOrder();
				Object[] keys = sorted.keySet().toArray();
				for(int i=0;i<sorted.size();i++){
					String pname = (String) keys[i];
					int pos = i+1;
				    Player pl = main.plugin.getServer().getPlayer(pname);
				    game.scores.getScore(pl).setScore(pos);
				    game.scoresBoard.getScore(pl).setScore(pos);
				}
				return;
			}}, this.scorerate, this.scorerate);
    	try {
			main.plugin.getServer().getPluginManager().callEvent(new RaceStartEvent(this));
		} catch (Exception e) {
			main.logger.log("Error starting race!", Level.SEVERE);
			end();
		}
    	return;
    }
    public SortedMap<String, Double> getRaceOrder(){
    	Race game = this;
    	HashMap<String, Double> checkpointDists = new HashMap<String, Double>();
		List<String> playerNames = game.getPlayers();
		for(String pname:playerNames){
			Player player = main.plugin.getServer().getPlayer(pname);
			if(player.hasMetadata("checkpoint.distance")){
				List<MetadataValue> metas = player.getMetadata("checkpoint.distance");
				checkpointDists.put(pname, (Double) ((StatValue)metas.get(0)).getValue());
			}
		}
    	Map<String,Double> scores = new HashMap<String,Double>();
		for(String pname:game.getPlayers()){
			int laps = game.totalLaps - game.lapsLeft.get(pname) +1;
			int checkpoints;
			try {
				checkpoints = game.checkpoints.get(pname);
			} catch (Exception e) {
				checkpoints = 0;
			}
			double distance = 1/(checkpointDists.get(pname));
			
			double score = (laps*game.getMaxCheckpoints()) + checkpoints + distance;
			try {
				if(game.getWinner().equals(pname)){
					score = score+1;
				}
			} catch (Exception e) {
			}
			scores.put(pname, score);
	    }
		DoubleValueComparator com = new DoubleValueComparator(scores);
    	SortedMap<String, Double> sorted = new TreeMap<String, Double>(com);
		sorted.putAll(scores);
		if(sorted.size() >= 1){
			this.winning = (String) sorted.keySet().toArray()[0];
		}
		return sorted;
    }
    public void end(){
    	this.running = false;
    	if(task != null){
    		task.cancel();
    	}
    	try {
			if(scoreCalcs != null){
				scoreCalcs.cancel();
			}
		} catch (Exception e1) {
		}
    	try {
			this.board.clearSlot(DisplaySlot.BELOW_NAME);
		} catch (IllegalArgumentException e) {
		}
    	try {
			this.board.clearSlot(DisplaySlot.SIDEBAR);
		} catch (IllegalArgumentException e) {
		}
    	try {
			this.scores.unregister();
			this.scoresBoard.unregister();
		} catch (IllegalStateException e) {
		}
        ArrayList<String> pls = new ArrayList<String>();
        pls.addAll(this.inplayers);
    	for(String playername:pls){
    		main.plugin.getServer().getPlayer(playername).setScoreboard(main.plugin.getServer().getScoreboardManager().getMainScoreboard());	
    		main.plugin.getServer().getPluginManager().callEvent(new RaceFinishEvent(this, playername));
    	}
    	RaceEndEvent evt = new RaceEndEvent(this);
    	main.plugin.getServer().getPluginManager().callEvent(evt);
    }
    public void finish(String playername){
    	if(!ending){
    		ending = true;
    		startEndCount();
    	}
    	this.finished.add(playername);
    	main.plugin.getServer().getPluginManager().callEvent(new RaceFinishEvent(this, playername));
    }
    public CheckpointCheck playerAtCheckpoint(Integer[] checks, Player p, Server server){
    	int checkpoint = 0;
    	Boolean at = false;
    	Map<Integer, SerializableLocation> schecks = this.track.getCheckpoints();
    	Location pl = p.getLocation();
    	for(Integer key: checks){
    		if(schecks.containsKey(key)){
    			SerializableLocation sloc = schecks.get(key);
    			Location check = sloc.getLocation(server);
    			double dist = check.distanceSquared(pl); //Squared because of better performance
    			p.removeMetadata("checkpoint.distance", main.plugin);
    			p.setMetadata("checkpoint.distance", new StatValue(dist, main.plugin));
        		if(dist < 100){
        			at = true;
        			checkpoint = key;
        			return new CheckpointCheck(at, checkpoint);
        		}		
    		}
    	}
    	return new CheckpointCheck(at, checkpoint);
    }
    public int getMaxCheckpoints(){
    	return maxCheckpoints; //Starts at 0
    }
    public Boolean atLine(Server server, Location loc){
    	Location line1 = this.track.getLine1(server);
    	Location line2 = this.track.getLine2(server);
    	String lineAxis = "x";
    	Boolean at = false;
    	Boolean l1 = true;
    	if(line1.getX()+0.5>line2.getX()-0.5 && line1.getX()-0.5<line2.getX()+0.5){
    		lineAxis = "z";
    	}
    	if(lineAxis == "x"){
    		if(line2.getX() < line1.getX()){
    			l1 = false;
    		}
    		if(l1){
    		    if(line2.getX()+0.5 > loc.getX() && loc.getX() > line1.getX()-0.5){
    			    at = true;
    		    }
    		}
    		else{
    			if(line1.getX()+0.5 > loc.getX() && loc.getX() > line2.getX()-0.5){
        			at = true;
        		}
    		}
    		if(at){
    			if(line1.getZ()+4 > loc.getZ() && line1.getZ()-4 < loc.getZ()){
    				if(line1.getY()+4 > loc.getY() && line1.getY()-4 < loc.getY()){
        				return true;
        			}
    			}
    		}
    	}
    	else if(lineAxis == "z"){
    		if(line2.getZ() < line1.getZ()){
    			l1 = false;
    		}
    		if(l1){
    		    if(line2.getZ()+0.5 > loc.getZ() && loc.getZ() > line1.getZ()-0.5){
    			    at = true;
    		    }
    		}
    		else{
    			if(line1.getZ()+0.5 > loc.getZ() && loc.getZ() > line2.getZ()-0.5){
        			at = true;
        		}
    		}
    		if(at){
    			if(line1.getX()+4 > loc.getX() && line1.getX()-4 < loc.getX()){
    				if(line1.getY()+4 > loc.getY() && line1.getY()-4 < loc.getY()){
        				return true;
        			}
    			}
    		}
    	}
    	
    	return false;
    }
}
