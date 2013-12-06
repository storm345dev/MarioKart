package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.mariokart.main;

import org.bukkit.entity.Player;

public class RaceQue {
	RaceTrack track = null;
	Boolean transitioning = false;
	int playerLimit = 2;
	RaceType type = RaceType.RACE;
	List<String> players = new ArrayList<String>();
	public RaceQue(RaceTrack track, RaceType type) {
		this.track = track;
		this.playerLimit = track.maxplayers;
		this.type = type;
	}

	public RaceType getType() {
		return this.type;
	}

	public RaceTrack getTrack() {
		return this.track;
	}

	public void setTransitioning(Boolean trans) {
		this.transitioning = trans;
		return;
	}

	public Boolean getTransitioning() {
		return this.transitioning;
	}

	public Boolean addPlayer(Player player) {
		if ((this.players.size() + 1) <= this.playerLimit) {
			this.players.add(player.getName());
			return true;
		} else {
			return false;
		}
	}

	public int getPlayerLimit() {
		return this.playerLimit;
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
		
		if (this.type == RaceType.TIME_TRIAL || this.getHowManyPlayers() < 1) {
			main.plugin.raceQues.removeQue(this.track.getTrackName());
		}
		
		return;
	}

	public void validatePlayers(Boolean checkPlayerCount) {
		for (String p : new ArrayList<String>(this.players)) {
			Player player = main.plugin.getServer().getPlayer(p);
			if (player == null || !player.isOnline()) {
				this.players.remove(player);
				if(checkPlayerCount){
				if (this.type == RaceType.TIME_TRIAL || this.getHowManyPlayers() < 1) {
					main.plugin.raceQues.removeQue(this.track.getTrackName());
				}
				}
			}
		}
		return;
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> ps = new ArrayList<Player>();
		validatePlayers(true);
		List<String> pp = new ArrayList<String>(this.players);
		for(String s:pp){
			ps.add(main.plugin.getServer().getPlayer(s));
		}
		return ps;
	}

	public int getHowManyPlayers() {
		validatePlayers(false);
		return this.players.size();
	}
	
	public void clear(){
		this.players.clear();
		return;
	}

}
