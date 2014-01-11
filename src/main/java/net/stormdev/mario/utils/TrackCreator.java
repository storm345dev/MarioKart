package net.stormdev.mario.utils;

import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.main;

import org.bukkit.entity.Player;

public class TrackCreator {
	Player player = null;
	RaceTrack track = null;
	int stage = 0;
	Boolean complete = false;
	Boolean completed = false;

	public TrackCreator(Player player, RaceTrack track) {
		this.player = player;
		this.track = track;
		stage = 0;
		main.trackCreators.put(player.getName(), this);
		start();
	}

	public void start() {
		String msg = main.msgs.get("setup.create.lobby");
		player.sendMessage(main.colors.getInfo() + msg);
		return;
	}

	public synchronized void set(Boolean wand) {
		if (complete) {
			saveTrack();
			return;
		}
		if (wand && stage == 0) {
			setLobby();
		} else if (wand && stage == 1) {
			setExit();
		} else if (!wand && stage == 2) {
			addGrid();
		} else if (wand && stage == 2) {
			finishGrid();
		} else if (!wand && stage == 3) {
			addCheckpoint();
		} else if (wand && stage == 3) {
			finishCheckpoints();
		} else if (wand && stage == 4) {
			setLine1();
		} else if (wand && stage == 5) {
			setLine2();
		}
		main.trackCreators.put(player.getName(), this);
		return;
	}

	public void setLobby() {
		track.setLobby(player.getLocation());
		String msg = main.msgs.get("setup.create.exit");
		player.sendMessage(main.colors.getInfo() + msg);
		stage = 1;
		return;
	}

	public void setExit() {
		track.setExit(player.getLocation());
		player.sendMessage(main.colors.getInfo()
				+ main.msgs.get("setup.create.grid"));
		stage = 2;
		return;
	}

	public void addGrid() {
		track.addToStartGrid(player.getLocation());
		player.sendMessage(main.colors.getInfo() + "["
				+ track.getStartGrid().size() + "]");
		return;
	}

	public void finishGrid() {
		player.sendMessage(main.colors.getInfo()
				+ main.msgs.get("setup.create.checkpoints"));
		stage = 3;
		return;
	}

	public void addCheckpoint() {
		int pos = track.getCheckpoints().size();
		track.addToCheckpoints(pos, player.getLocation());
		player.sendMessage(main.colors.getInfo() + "["
				+ track.getCheckpoints().size() + "]");
		return;
	}

	public void finishCheckpoints() {
		int amount = track.getCheckpoints().size();
		if (amount < 3) {
			String msg = main.msgs.get("setup.create.notEnoughCheckpoints");
			msg = msg.replaceAll(Pattern.quote("%num%"), amount + "");
			player.sendMessage(main.colors.getError() + msg);
			return;
		}
		player.sendMessage(main.colors.getInfo()
				+ main.msgs.get("setup.create.line1"));
		stage = 4;
		return;
	}

	public void setLine1() {
		track.setLine1(player.getLocation());
		player.sendMessage(main.colors.getInfo()
				+ main.msgs.get("setup.create.line2"));
		stage = 5;
		return;
	}

	public void setLine2() {
		track.setLine2(player.getLocation());
		stage = 6;
		saveTrack();
		return;
	}

	public synchronized void saveTrack() {
		if(completed){
			return;
		}
		completed = true;
		main.plugin.trackManager.setRaceTrack(track.getTrackName(), track);
		String msg = main.msgs.get("setup.create.done");
		msg = msg.replaceAll(Pattern.quote("%name%"), track.getTrackName());
		player.sendMessage(main.colors.getSuccess() + msg);
		main.trackCreators.remove(player.getName());
		return;
	}
}
