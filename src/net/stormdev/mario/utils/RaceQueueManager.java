package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public RaceQueue getQueue(String trackName, UUID queueId){
		Map<UUID, RaceQueue> trackQueues = getQueues(trackName);
		if(trackQueues.size() < 1){
			return null;
		}
		Set<UUID> keys = trackQueues.keySet();
		for(UUID key:keys){
			RaceQueue r = trackQueues.get(key);
			if(r.getQueueId() == queueId){
				return r;
			}
		}
		return null;
	}
	
	public Map<UUID, RaceQueue> getQueues(RaceType type){
		Map<UUID, RaceQueue> trackQueues = getAllQueues();
		for(UUID id:new ArrayList<UUID>(trackQueues.keySet())){
			RaceQueue queue = trackQueues.get(id);
			if(queue.getRaceMode() != type){
				trackQueues.remove(queue);
			}
		}
		return trackQueues;
	}
	
	public Map<UUID, RaceQueue> getOpenQueues(RaceType type){
		Map<UUID, RaceQueue> trackQueues = getAllQueues();
		for(UUID id:new ArrayList<UUID>(trackQueues.keySet())){
			RaceQueue queue = trackQueues.get(id);
			if(queue.getRaceMode() != type || queue.playerCount()>=queue.playerLimit()){
				trackQueues.remove(queue.getQueueId());
			}
		}
		return trackQueues;
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
		List<UUID> keys = new ArrayList<UUID>(trackQueues.keySet());
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
	
	public Map<UUID, RaceQueue> getAllQueues(){
		List<String> tracks = new ArrayList<String>(main.plugin.queues.keySet());
		Map<UUID, RaceQueue> queues = new HashMap<UUID, RaceQueue>();
		for(String tName:tracks){
			queues.putAll(getQueues(tName));
		}
		return queues;
	}
	
	public void updateQueue(RaceQueue queue){
		Map<UUID, RaceQueue> trackQueues = new HashMap<UUID, RaceQueue>();
		if(main.plugin.queues.containsKey(queue.getTrackName())){
			trackQueues = main.plugin.queues.get(queue.getTrackName());
		}
		if(trackQueues.containsKey(queue.getQueueId())){
			trackQueues.put(queue.getQueueId(), queue);
			main.plugin.queues.put(queue.getTrackName(), trackQueues);
		}
		return;
	}
	
	public void clear(){
		Map<UUID, RaceQueue> queues = getAllQueues();
		for(UUID id:queues.keySet()){
			RaceQueue q = ((RaceQueue)queues.get(id));
			q.clear();
			main.plugin.queues.remove(id);
		}
	}

}
