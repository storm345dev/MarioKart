package net.stormdev.mario.events;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.queues.RaceQueue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class QueueEventsListener implements Listener {
	@SuppressWarnings("unused")
	private MarioKart plugin;
	
	public QueueEventsListener(MarioKart plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void queueRespawns(PlayerRespawnEvent event) { //Handle respawns while in a queue
		Player player = event.getPlayer();
		RaceQueue r = MarioKart.plugin.raceMethods.inGameQue(player);
		if (r == null) {
			return;
		}
		event.setRespawnLocation(r.getTrack().getLobby(MarioKart.plugin.getServer()));
	}
}
