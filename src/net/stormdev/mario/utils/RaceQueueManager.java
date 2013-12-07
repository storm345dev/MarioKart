package net.stormdev.mario.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.stormdev.mario.mariokart.main;

public class RaceQueueManager {
	
	public RaceQueueManager(){
	}
	
	public Boolean queueExists(String trackName, RaceType raceMode){
		Map<UUID, RaceQueue> trackQueues = getQueues(trackName);
		if(trackQueues.size() < 1){
			return false;
		}
		Set<UUID> keys = trackQueues.keySet();
		for(UUID key:keys){
			RaceQueue r = trackQueues.get(key);
			if(r.getRaceMode() == raceMode){
				return true;
			}
		}
		return false;
	}
	
	public RaceQueue getQueue(String trackName, RaceType raceMode){
		Map<UUID, RaceQueue> trackQueues = getQueues(trackName);
		if(trackQueues.size() < 1){
			return null;
		}
		Set<UUID> keys = trackQueues.keySet();
		for(UUID key:keys){
			RaceQueue r = trackQueues.get(key);
			if(r.getRaceMode() == raceMode){
				return r;
			}
		}
		return null;
	}
	
	public Map<UUID, RaceQueue> getQueues(String trackName){
		Map<UUID, RaceQueue> trackQueues = new HashMap<UUID, RaceQueue>();
		if(main.plugin.queues.containsKey(trackName)){
			trackQueues = main.plugin.queues.get(trackName);
		}
		return trackQueues;
	}
	
	public void removeQueue(String trackName, UUID queueId){
		Map<UUID, RaceQueue> trackQueues = new HashMap<UUID, RaceQueue>();
		if(main.plugin.queues.containsKey(trackName)){
			trackQueues = main.plugin.queues.get(trackName);
		}
		if(trackQueues.size() < 1){
			return;
		}
		Set<UUID> keys = trackQueues.keySet();
		for(UUID key:keys){
			RaceQueue r = trackQueues.get(key);
			if(r.getQueueId() == queueId){
				removeQueue(r);
				return;
			}
		}
	}
	
	public void removeQueue(RaceQueue queue){
		queue.clear();
		Map<UUID, RaceQueue> trackQueues = new HashMap<UUID, RaceQueue>();
		if(main.plugin.queues.containsKey(queue.getTrackName())){
			trackQueues = main.plugin.queues.get(queue.getTrackName());
		}
		trackQueues.remove(queue.getQueueId());
		main.plugin.queues.put(queue.getTrackName(), trackQueues);	
	}

}
