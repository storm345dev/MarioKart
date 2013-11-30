package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.main;

public class RaceMethods {
	private main plugin = null;

	public RaceMethods() {
		this.plugin = main.plugin;
	}

	public Race inAGame(String playername) {
		HashMap<String, Race> games = plugin.gameScheduler.getGames();
		Set<String> keys = games.keySet();
		Boolean inAGame = false;
		Race mgame = null;
		for (String key : keys) {
			Race game = games.get(key);
			if (game.getInPlayers().contains(playername)) {
				inAGame = true;
				mgame = game;
			}
		}
		if (inAGame) {
			return mgame;
		}
		return null;
	}

	public String inGameQue(String playername) {
		Set<String> arenaNames = plugin.raceQues.getQues();
		for (String arenaName : arenaNames) {
			try {
				List<String> que = plugin.raceQues.getQue(arenaName)
						.getPlayers();
				if (que.contains(playername)) {
					return arenaName;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
