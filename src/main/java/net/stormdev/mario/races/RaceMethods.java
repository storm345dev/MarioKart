package net.stormdev.mario.races;

import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.queues.RaceQueue;

import org.bukkit.entity.Player;

public class RaceMethods {
	@SuppressWarnings("unused")
	private MarioKart plugin = null;

	public RaceMethods() {
		this.plugin = MarioKart.plugin;
	}

	public synchronized Race inAGame(Player player, Boolean update) {
		return MarioKart.plugin.raceScheduler.inAGame(player, update);
	}

	public synchronized RaceQueue inGameQue(Player player) {
		Map<UUID, RaceQueue> queues = MarioKart.plugin.raceQueues.getAllQueues();
		for (UUID id : queues.keySet()) {
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
