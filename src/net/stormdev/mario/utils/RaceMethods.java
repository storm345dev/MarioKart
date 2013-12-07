package net.stormdev.mario.utils;

import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.mariokart.Race;
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

	public RaceQueue inGameQue(Player player) {
		Map<UUID, RaceQueue> queues = plugin.raceQueues.getAllQueues();
		for (UUID id:queues.keySet()) {
			try {
				RaceQueue queue = queues.get(id);
				if (queue.containsPlayer(player)) {
					return queue;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
