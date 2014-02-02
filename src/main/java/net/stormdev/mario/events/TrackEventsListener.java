package net.stormdev.mario.events;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.tracks.TrackCreator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TrackEventsListener implements Listener {
	@SuppressWarnings("unused")
	private MarioKart plugin;
	
	public TrackEventsListener(MarioKart plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onWandClickEvent(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)
				&& !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Player player = event.getPlayer();
		if (!TrackCreator.trackCreators.containsKey(player.getName())) {
			return;
		}
		TrackCreator creator = TrackCreator.trackCreators.get(player.getName());
		Boolean wand = false;
		@SuppressWarnings("deprecation")
		int handid = player.getItemInHand().getTypeId();
		if (handid == MarioKart.config.getInt("setup.create.wand")) {
			wand = true;
		}
		creator.set(wand);
		return;
	}
}
