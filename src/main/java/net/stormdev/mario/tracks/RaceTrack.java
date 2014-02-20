package net.stormdev.mario.tracks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.Location;
import org.bukkit.Server;

public class RaceTrack implements Serializable {
	private static final long serialVersionUID = -2618235864567541066L;
	double classVersion = 1;
	String trackname = "Unknown";
	int maxplayers = 10;
	int minplayers = -1;
	public int laps = 3;
	SerializableLocation lobby = null;
	SerializableLocation exit = null;
	SerializableLocation line1 = null;
	SerializableLocation line2 = null;
	ArrayList<SerializableLocation> startGrid = new ArrayList<SerializableLocation>();
	private Map<Integer, SerializableLocation> checkPoints = new HashMap<Integer, SerializableLocation>();

	public RaceTrack(String trackname, int maxplayers, int minplayers, int laps) {
		this.trackname = trackname;
		this.maxplayers = maxplayers;
		this.minplayers = minplayers;
		this.laps = laps;
	}
	
	public int getMinPlayers(){
		if(minplayers > 0){
			return minplayers; //Use track specific setting
		}
		return MarioKart.config
				.getInt("race.que.minPlayers"); //Return default;
	}

	public int getLaps() {
		return laps;
	}

	public void setLine1(Location loc) {
		SerializableLocation sloc = new SerializableLocation(loc);
		this.line1 = sloc;
	}

	public Location getLine1(Server server) {
		return this.line1.getLocation(server);
	}

	public void setLine2(Location loc) {
		SerializableLocation sloc = new SerializableLocation(loc);
		this.line2 = sloc;
	}

	public Location getLine2(Server server) {
		return this.line2.getLocation(server);
	}

	public void setLobby(Location loc) {
		SerializableLocation sloc = new SerializableLocation(loc);
		this.lobby = sloc;
	}

	public Location getLobby(Server server) {
		return this.lobby.getLocation(server);
	}

	public void setExit(Location loc) {
		SerializableLocation sloc = new SerializableLocation(loc);
		this.exit = sloc;
	}

	public Location getExit(Server server) {
		return this.exit.getLocation(server);
	}

	public void setTrackName(String trackname) {
		this.trackname = trackname;
		return;
	}

	public void setMaxPlayers(int maxplayers) {
		this.maxplayers = maxplayers;
		return;
	}

	public void setMinPlayers(int minplayers) {
		this.minplayers = minplayers;
		return;
	}

	public String getTrackName() {
		return this.trackname;
	}

	public int getMaxPlayers() {
		return this.maxplayers;
	}

	final public ArrayList<SerializableLocation> getStartGrid() {
		final ArrayList<SerializableLocation> s = this.startGrid;
		return s;
	}

	public void setStartGrid(ArrayList<SerializableLocation> startGrid) {
		this.startGrid = startGrid;
		calculateMaxMinPlayers();
		return;
	}

	public void addToStartGrid(SerializableLocation loc) {
		this.startGrid.add(loc);
		calculateMaxMinPlayers();
	}

	public void addToStartGrid(Location loc) {
		SerializableLocation sloc = new SerializableLocation(loc);
		this.startGrid.add(sloc);
		calculateMaxMinPlayers();
	}

	public void RemoveFromStartGrid(SerializableLocation loc) {
		this.startGrid.remove(loc);
		calculateMaxMinPlayers();
	}
	
	public synchronized int countCheckPoints(){
		return checkPoints.size();
	}
	
	public SerializableLocation getCheckpoint(int i){
		return checkPoints.get(i);
	}
	
	public synchronized Map<Integer, Location> loadCheckpoints(Server server){
		Map<Integer, Location> locs = new HashMap<Integer, Location>();
		for(Integer i:checkPoints.keySet()){
			locs.put(i, checkPoints.get(i).getLocation(server));
		}
		
		return locs;
	}

	public synchronized void addToCheckpoints(int num, SerializableLocation loc) {
		this.checkPoints.put(num, loc);
		calculateMaxMinPlayers();
	}

	public synchronized void addToCheckpoints(int num, Location loc) {
		SerializableLocation sloc = new SerializableLocation(loc);
		this.checkPoints.put(num, sloc);
		calculateMaxMinPlayers();
	}

	public synchronized void RemoveFromCheckpoints(SerializableLocation loc) {
		this.checkPoints.remove(loc);
		calculateMaxMinPlayers();
	}

	public void calculateMaxMinPlayers() {
		if (this.startGrid.size() > 1) {
			this.minplayers = 2;
		} else {
			this.minplayers = 1;
		}
		this.maxplayers = this.startGrid.size();
		return;
	}
}
