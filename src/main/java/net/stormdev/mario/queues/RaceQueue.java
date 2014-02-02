package net.stormdev.mario.queues;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.tracks.RaceTrack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RaceQueue {
	private RaceTrack track = null;
	private Boolean starting = false;
	private int playerLimit = 2;
	private RaceType type = RaceType.RACE;
	private List<Player> players = new ArrayList<Player>();
	private UUID queueId = null;

	public RaceQueue(RaceTrack track, RaceType type, Player creator) {
		this.track = track;
		if(type == RaceType.AUTO){
			if((MarioKart.plugin.random.nextBoolean() && MarioKart.plugin.random.nextBoolean())
					|| Bukkit.getOnlinePlayers().length < MarioKart.config.getInt("race.que.minPlayers")){
				type = RaceType.TIME_TRIAL;
			}
			else{
				type = RaceType.RACE;
			}
		}
		this.type = type;
		this.queueId = UUID.randomUUID();
		this.playerLimit = track.getMaxPlayers();
		this.players.add(creator);
		LinkedHashMap<UUID, RaceQueue> trackQueues = new LinkedHashMap<UUID, RaceQueue>();
		if (MarioKart.plugin.queues.containsKey(getTrackName())) {
			trackQueues = MarioKart.plugin.queues.get(getTrackName());
		}
		trackQueues.put(queueId, this);
		MarioKart.plugin.queues.put(getTrackName(), trackQueues); // Queue is now
																// registered
																// with the
																// system
	}

	public String getTrackName() {
		return track.getTrackName();
	}

	public void setTrack(RaceTrack track) {
		this.track = track;
		this.playerLimit = track.getMaxPlayers();
		MarioKart.plugin.raceQueues.updateQueue(this);
	}

	public RaceTrack getTrack() {
		return track;
	}

	public Boolean isStarting() {
		return starting;
	}

	public void setStarting(Boolean starting) {
		this.starting = starting;
		MarioKart.plugin.raceQueues.updateQueue(this);
		MarioKart.plugin.raceScheduler.recalculateQueues();
		return;
	}

	public int getMaxPlayers() {
		return playerLimit;
	}

	public RaceType getRaceMode() {
		return type;
	}

	public void setRaceMode(RaceType type) {
		this.type = type;
		MarioKart.plugin.raceQueues.updateQueue(this);
	}

	public UUID getQueueId() {
		return queueId;
	}

	public void regenQueueId() {
		this.queueId = UUID.randomUUID();
		MarioKart.plugin.raceQueues.updateQueue(this);
	}

	public Boolean validatePlayers() {
		Boolean valid = true;
		ArrayList<String> leftPlayers = new ArrayList<String>();
		try {
			for (Player p : getPlayers()) {
				if (p == null || !p.isOnline()) {
					players.remove(p);
					leftPlayers.add(p.getName());
				}
			}
		} catch (Exception e) {
			// Error checking if players valid
		}
		if (players.size() < 1) {
			valid = false;
		}
		if (!valid) { // If there's not enough players in the queue
			clear();
			LinkedHashMap<UUID, RaceQueue> trackQueues = new LinkedHashMap<UUID, RaceQueue>();
			if (MarioKart.plugin.queues.containsKey(getTrackName())) {
				trackQueues = MarioKart.plugin.queues.get(getTrackName());
			}
			trackQueues.remove(queueId);
			MarioKart.plugin.queues.put(getTrackName(), trackQueues); // Queue is now
																	// un-registered
																	// with the
																	// system
			return false;
		}
		for (String s : leftPlayers) {
			broadcast(MarioKart.colors.getTitle() + "[MarioKart:] "
					+ MarioKart.colors.getInfo() + s
					+ MarioKart.msgs.get("race.que.left"));
		}
		return true;
	}

	public int playerCount() {
		try {
			validatePlayers();
		} catch (Exception e) {
			//Game voided
		}
		return players.size();
	}
	
	public int currentPlayerCount() {
		return players.size();
	}

	public int playerLimit() {
		return playerLimit;
	}

	public List<Player> getPlayers() {
		return new ArrayList<Player>(players);
	}

	public Boolean addPlayer(Player player) {
		if (player != null && player.isOnline()
				&& (playerCount() < playerLimit)) {
			players.add(player);
			MarioKart.plugin.raceQueues.updateQueue(this);
			MarioKart.plugin.raceScheduler.recalculateQueues();
			MarioKart.plugin.signManager.updateSigns(getTrack());
			return true;
		}
		return false;
	}

	public void removePlayer(Player player) {
		players.remove(player);
		validatePlayers();
		broadcast(MarioKart.colors.getTitle() + "[MarioKart:] "
				+ MarioKart.colors.getInfo() + player.getName()
				+ MarioKart.msgs.get("race.que.left"));
		MarioKart.plugin.raceQueues.updateQueue(this);
		MarioKart.plugin.signManager.updateSigns(getTrack());
		MarioKart.plugin.raceScheduler.recalculateQueues();
	}

	public void removePlayer(String player) {
		for (Player p : getPlayers()) {
			if (p.getName().equals(player)) {
				MarioKart.plugin.signManager.updateSigns(getTrack());
				removePlayer(p);
				return;
			}
		}
	}
	
	public void quietRemovePlayer(String player) {
		for (Player p : getPlayers()) {
			if (p.getName().equals(player)) {
				removePlayer(p);
				return;
			}
		}
	}

	public void clear() {
		this.players.clear();
		this.type = RaceType.RACE;
		starting = false;
		MarioKart.plugin.raceQueues.updateQueue(this);
	}

	public Boolean containsPlayer(Player player) {
		return players.contains(player);
	}

	public void broadcast(String message) {
		validatePlayers();
		for (Player p : getPlayers()) {
			p.sendMessage(MarioKart.colors.getInfo() + message);
		}
		return;
	}

}
