package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.utils.RaceQueue;
import net.stormdev.mario.utils.RaceTrack;
import net.stormdev.mario.utils.RaceType;

import org.bukkit.entity.Player;

public class RaceScheduler {
	private HashMap<UUID, Race> races = new HashMap<UUID, Race>();
	private int raceLimit = 5;
	public RaceScheduler(int raceLimit){
		this.raceLimit = raceLimit;
	}
	public void joinAutoQueue(Player player, RaceType type){
		Map<UUID, RaceQueue> queues = main.plugin.raceQueues.getOpenQueues(type); //Joinable queues for that racemode
		RaceQueue toJoin = null;
		if(queues.size() > 0){
		int targetPlayers = main.config.getInt("general.race.targetPlayers");
		Map<UUID, RaceQueue> recommendedQueues = new HashMap<UUID, RaceQueue>();
		for(UUID id:new ArrayList<UUID>(queues.keySet())){
			RaceQueue queue = queues.get(id);
			if(queue.playerCount() < targetPlayers){
				recommendedQueues.put(id, queue);
			}
		}
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
			//Create a random queue
			List<RaceTrack> tracks = main.plugin.trackManager.getRaceTracks();
			List<RaceTrack> openTracks = new ArrayList<RaceTrack>();
			List<RaceTrack> clearQueuedTracks = new ArrayList<RaceTrack>();
			for(RaceTrack t:tracks){
				if(!isTrackInUse(t, type)){
					openTracks.add(t);
					if(main.plugin.raceQueues.getQueues(t.getTrackName()).size() < 1){
						clearQueuedTracks.add(t);
					}
				}
			}
			RaceTrack track= null;
			if(clearQueuedTracks.size() > 0 && type != RaceType.TIME_TRIAL){
				track = clearQueuedTracks.get(main.plugin.random.nextInt(clearQueuedTracks.size()));
			}
			else{
				if(openTracks.size() > 0){
					// - They're going to have to wait for another race to finish before them...
					track = openTracks.get(main.plugin.random.nextInt(openTracks.size()));
				}
				else{
					if(type == RaceType.TIME_TRIAL && clearQueuedTracks.size() > 0){
						//Put them on a track to themselves
						track = clearQueuedTracks.get(main.plugin.random.nextInt(clearQueuedTracks.size()));
					}
					else{
						if(tracks.size() < 1){						
							//No tracks exist
							// No tracks created
							player.sendMessage(main.colors.getError()
									+ main.msgs.get("general.cmd.full"));
							return;
						}
						track = tracks.get(main.plugin.random.nextInt(tracks.size()));
					    //-They are going to have to wait for a game to finish
					}
				}
			}
			toJoin = new RaceQueue(track, type);
		}
		//Join that queue
		toJoin.addPlayer(player);
	}
	
	public void joinQueue(Player player, RaceTrack track, RaceType type){
		RaceQueue queue = main.plugin.raceQueues.getQueue(track.getTrackName(), type); //Get the oldest queue of that type for that track
		if(queue == null){
			queue = new RaceQueue(track, type);
		}
		queue.addPlayer(player);
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
	
	public Boolean isTrackInUse(RaceTrack track, RaceType type){
		HashMap<UUID, Race> rs = new HashMap<UUID, Race>(races);
		for(UUID id:rs.keySet()){
			Race r = rs.get(id);
			if(r.getTrackName().equals(track.getTrackName())){
				if(type == RaceType.TIME_TRIAL && r.getType() == RaceType.TIME_TRIAL){
					return false;
				}
				return true;
			}
		}
		return false;
	}

}
