package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.mariokart.main;

import org.bukkit.entity.Player;

public class RaceQueue {
	private RaceTrack track = null;
    private Boolean starting = false;
    private int playerLimit = 2;
    private RaceType type = RaceType.RACE;
    private List<Player> players = new ArrayList<Player>();
    private UUID queueId = null;
	public RaceQueue(RaceTrack track, RaceType type){
		this.track = track;
		this.type = type;
		this.queueId = UUID.randomUUID();
		this.playerLimit = track.getMaxPlayers();
		Map<UUID, RaceQueue> trackQueues = new HashMap<UUID, RaceQueue>();
		if(main.plugin.queues.containsKey(getTrackName())){
			trackQueues = main.plugin.queues.get(getTrackName());
		}
		trackQueues.put(queueId, this);
		main.plugin.queues.put(getTrackName(), trackQueues); //Queue is now registered with the system
	}
	public String getTrackName(){
		return track.getTrackName();
	}
	
	public void setTrack(RaceTrack track){
		this.track = track;
		this.playerLimit = track.getMaxPlayers();
	}
	
	public RaceTrack getTrack(){
		return track;
	}
	
	public Boolean isStarting(){
		return starting;
	}
	
	public int getMaxPlayers(){
		return playerLimit;
	}
	
	public RaceType getRaceMode(){
		return type;
	}
	
	public void setRaceMode(RaceType type){
		this.type = type;
	}
	
	public UUID getQueueId(){
		return queueId;
	}
	
	public void regenQueueId(){
		this.queueId = UUID.randomUUID();
	}
	
	public void validatePlayers(){	
		List<Player> pls = new ArrayList<Player>(players)
	}
	

}
