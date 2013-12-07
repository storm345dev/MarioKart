package net.stormdev.mario.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.User;
import net.stormdev.mario.mariokart.main;

import org.bukkit.entity.Player;

public class RaceMethods {
	private main plugin = null;

	public RaceMethods() {
		this.plugin = main.plugin;
	}

	public Race inAGame(Player player) {
		//TODO
		return null;
	}

	public String inGameQue(Player player) {
		Set<String> arenaNames = plugin.raceQueues.getAllQueues();
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
