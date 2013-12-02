package net.stormdev.mario.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.User;
import net.stormdev.mario.mariokart.main;

public class RaceMethods {
	private main plugin = null;

	public RaceMethods() {
		this.plugin = main.plugin;
	}

	public Race inAGame(Player player) {
		HashMap<String, Race> games = plugin.gameScheduler.getGames();
		Set<String> keys = games.keySet();
		Boolean inAGame = false;
		Race mgame = null;
		for (String key : keys) {
			Race game = games.get(key);
			for (User user : game.getUsersIn()){
				if (user.getPlayerName().equals(player.getName())){
					inAGame = true;
					mgame = game;
				}
			}
		}
		if (inAGame) {
			return mgame;
		}
		return null;
	}

	public String inGameQue(Player player) {
		Set<String> arenaNames = plugin.raceQues.getQues();
		for (String arenaName : arenaNames) {
			try {
				List<Player> que = plugin.raceQues.getQue(arenaName).getPlayers();
				if (que.contains(player)) {
					return arenaName;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
