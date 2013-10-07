package net.stormdev.ucars.utils;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.race.main;

public class RaceQue {
	RaceTrack track = null;
	Boolean transitioning = false;
	int playerLimit = 2;
	List<String> players = new ArrayList<String>();
	public RaceQue(RaceTrack track){
		this.track = track;
		this.playerLimit = track.maxplayers;
	}
	public RaceTrack getTrack(){
		return this.track;
	}
	public void setTransitioning(Boolean trans){
		this.transitioning = trans;
		return;
	}
	public Boolean getTransitioning(){
		return this.transitioning;
	}
	public Boolean addPlayer(String name){
		if((this.players.size() + 1) <= this.playerLimit){
			this.players.add(name);
			return true;
		}
		else{
		return false;
		}
	}
	public int getPlayerLimit(){
		return this.playerLimit;
	}
	public void removePlayer(String name){
		this.players.remove(name);
		return;
	}
	public void validatePlayers(){
		for(String pname:this.players){
			if(main.plugin.getServer().getPlayer(pname) == null){
				this.players.remove(pname);
			}
		}
		return;
	}
	public List<String> getPlayers(){
		for(String pname:this.players){
			if(main.plugin.getServer().getPlayer(pname) == null){
				this.players.remove(pname);
			}
		}
		return this.players;
	}
	public int getHowManyPlayers(){
		for(String pname:this.players){
			if(main.plugin.getServer().getPlayer(pname) == null && !(main.plugin.getServer().getPlayer(pname).isOnline())){
				this.players.remove(pname);
			}
		}
		return this.players.size();
	}
	
}
