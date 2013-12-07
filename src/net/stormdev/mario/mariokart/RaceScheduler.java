package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.utils.RaceQueue;
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
		Map<UUID, RaceQueue> queues = main.plugin.raceQueues.getOpenQueues(type); //Joinable queues for that racemode
		if(queues.size() > 0){
		int targetPlayers = main.config.getInt("general.race.targetPlayers");
		Map<UUID, RaceQueue> recommendedQueues = new HashMap<UUID, RaceQueue>();
		for(UUID id:new ArrayList<UUID>(queues.keySet())){
			RaceQueue queue = queues.get(id);
			if(queue.playerCount() < targetPlayers){
				recommendedQueues.put(id, queue);
			}
		}
		RaceQueue toJoin = null;
		if(recommendedQueues.size() > 0){
			UUID random = (UUID) recommendedQueues.keySet().toArray()
					[main.plugin.random.nextInt(recommendedQueues.size())];
			toJoin = recommendedQueues.get(random); 
		}
		else{
			//Join from 'queues'
			UUID random = (UUID) queues.keySet().toArray()
					[main.plugin.random.nextInt(queues.size())];
			toJoin = queues.get(random); 
		}
		}
		else{
			//TODO Create a random queue
		}
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
	
	public void stopRace(Race race){
		//TODO
	}
	
	public void removeRace(Race race){
		this.races.remove(race);
	}
	
	public void updateRace(Race race){
		if(this.races.containsKey(race.getGameId())){
			this.races.put(race.getGameId(), race);
		}
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
