package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.stormdev.mario.mariokart.main;

public class RaceQue {
	RaceTrack track = null;
	Boolean transitioning = false;
	int playerLimit = 2;
	RaceType type = RaceType.RACE;
	List<Player> players = new ArrayList<Player>();

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
			this.players.add(player);
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
		
		if (this.type == RaceType.TIME_TRIAL) {
			main.plugin.raceQues.removeQue(this.track.getTrackName());
		}
		
		return;
	}

	public void validatePlayers() {
		for (Player player : this.players) {
			if (player == null) {
				this.players.remove(player);
				
				if (this.type == RaceType.TIME_TRIAL) {
					main.plugin.raceQues.removeQue(this.track.getTrackName());
				}
			}
		}
		return;
	}

	public List<Player> getPlayers() {
		for (Player player : this.players) {
			if (player == null) {
				this.players.remove(player);
			}
		}
		return new ArrayList<Player>(this.players);
	}

	public int getHowManyPlayers() {
		for (Player player : this.players) {
			if (player == null || !player.isOnline()) {
				this.players.remove(player);
			}
		}
		return this.players.size();
	}

}
