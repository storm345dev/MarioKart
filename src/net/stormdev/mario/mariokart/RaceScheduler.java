package net.stormdev.mario.mariokart;

import java.util.HashMap;
import java.util.UUID;

import net.stormdev.mario.utils.RaceType;

import org.bukkit.entity.Player;

public class RaceScheduler {
	private HashMap<UUID, Race> races = new HashMap<UUID, Race>();
	private int raceLimit = 5;
	public RaceScheduler(int raceLimit){
		this.raceLimit = raceLimit;
	}
	public void joinAutoQueue(Player player, RaceType type){
		//TODO
	}
	
	public void joinQueue(Player player, String trackName, RaceType type){
		//TODO
	}
	
	public void leaveQueue(Player player){
		//TODO
	}
	
	public void recalculateQueues(){
		//TODO
	}
	
	public void startRace(){
		//TODO
	}
	
	public void stopRace(){
		//TODO
	}
	
	public HashMap<UUID, Race> getRaces(){
		//TODO
		return new HashMap<UUID, Race>(races);
	}
	
	public int getRacesRunning(){
		//TODO
		return races.size();
	}

}
