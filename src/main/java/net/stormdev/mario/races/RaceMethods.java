package net.stormdev.mario.races;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.mariokart.User;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.queues.RaceQueue;

import org.bukkit.entity.Player;

public class RaceMethods {
	private main plugin = null;

	public RaceMethods() {
		this.plugin = main.plugin;
	}

	public synchronized Race inAGame(Player player, Boolean update) {
		Map<UUID, Race> races = main.plugin.raceScheduler.getRaces();
		for (UUID id : new ArrayList<UUID>(races.keySet())) {
			Race r = races.get(id);
			if (update) {
				r.updateUser(player);
			}
			List<User> users = r.getUsersIn(); // Exclude those that have
												// finished the race
			for (User u : users) {
				if (u.getPlayerName().equals(player.getName())) {
					return r;
				}
			}
		}
		return null;
	}

	public synchronized RaceQueue inGameQue(Player player) {
		Map<UUID, RaceQueue> queues = main.plugin.raceQueues.getAllQueues();
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
