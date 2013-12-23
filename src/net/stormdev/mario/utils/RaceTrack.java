package net.stormdev.mario.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;
import org.bukkit.Server;

public class RaceTrack implements Serializable {
	private static final long serialVersionUID = -2618235864567541066L;
	String trackname = "Unknown";
	int maxplayers = 10;
	int minplayers = 2;
	public int laps = 3;
	SerializableLocation lobby = null;
	SerializableLocation exit = null;
	SerializableLocation line1 = null;
	SerializableLocation line2 = null;
	ArrayList<SerializableLocation> startGrid = new ArrayList<SerializableLocation>();
	ConcurrentMap<Integer, SerializableLocation> checkPoints = new ConcurrentHashMap<Integer, SerializableLocation>();

	public RaceTrack(String trackname, int maxplayers, int minplayers, int laps) {
		this.trackname = trackname;
		this.maxplayers = maxplayers;
		this.minplayers = minplayers;
		this.laps = laps;
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

	public int getMinPlayers() {
		return this.minplayers;
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

	final public Map<Integer, SerializableLocation> getCheckpoints() {
		return checkPoints;
	}

	public void setCheckpoints(ConcurrentMap<Integer, SerializableLocation> checkpoints) {
		this.checkPoints = checkpoints;
		calculateMaxMinPlayers();
		return;
	}

	public void addToCheckpoints(int num, SerializableLocation loc) {
		this.checkPoints.put(num, loc);
		calculateMaxMinPlayers();
	}

	public void addToCheckpoints(int num, Location loc) {
		SerializableLocation sloc = new SerializableLocation(loc);
		this.checkPoints.put(num, sloc);
		calculateMaxMinPlayers();
	}

	public void RemoveFromCheckpoints(SerializableLocation loc) {
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
