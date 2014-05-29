package net.stormdev.mario.events;

import net.stormdev.mario.lesslag.DynamicLagReducer;
import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.races.Race;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ServerEventsListener implements Listener {
	private MarioKart plugin;
	
	public ServerEventsListener(MarioKart plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	void villagerInteract(PlayerInteractEntityEvent event){
		Entity interact = event.getRightClicked();
		if(!interact.getType().equals(EntityType.VILLAGER)){
			return;
		}
		Villager vil = (Villager) interact;
		if(!ChatColor.stripColor(vil.getCustomName()).equals("Race Shop")){
			return;
		}
		event.setCancelled(true);
		event.getPlayer().performCommand("race shop");
	}
	
	@EventHandler
	void villagerDamage(EntityDamageEvent event){
		if(event.getEntity().getType().equals(EntityType.VILLAGER)){
			Villager villager = (Villager) event.getEntity();
			if(!ChatColor.stripColor(villager.getCustomName()).equals("Race Shop")){
				return;
			}
			event.setDamage(0);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	void gameQuitting(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		MarioKart.plugin.resourcedPlayers.remove(player.getName());
		Race game = plugin.raceMethods.inAGame(player, false);
		if (game == null) {
			RaceQueue queue = plugin.raceMethods.inGameQue(player);
			if (queue == null) {
				return;
			}
			queue.removePlayer(player);
			return;
		} else {
			game.leave(game.getUser(player.getName()), true);
			return;
		}
	}

	@EventHandler
	void gameQuitting(PlayerKickEvent event) {
		Player player = event.getPlayer();
		MarioKart.plugin.resourcedPlayers.remove(player.getName());
		Race game = plugin.raceMethods.inAGame(player, false);
		if (game == null) {
			RaceQueue queue = plugin.raceMethods.inGameQue(player);
			if (queue == null) {
				return;
			}
			queue.removePlayer(player);
			return;
		} else {
			game.leave(game.getUser(player.getName()), true);
			return;
		}
	}
	
	@EventHandler
	void crystalExplode(EntityExplodeEvent event) { //Stop item boxes being destroyed
		if (!(event.getEntity() instanceof EnderCrystal)) {
			return;
		}
		Entity crystal = event.getEntity();
		// if(crystal.hasMetadata("race.pickup")){
		event.setCancelled(true);
		event.setYield(0);
		Location newL = crystal.getLocation();
		if(!MarioKart.powerupManager.spawnItemPickupBox(newL)){
			crystal.remove();
		}

		return;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	void stopCrystalFire(BlockIgniteEvent event) { //Stop item boxes setting fire
		if (event.getCause() != IgniteCause.ENDER_CRYSTAL) {
			return;
		}
		event.setCancelled(true);
		return;
	}
	

	@EventHandler(priority = EventPriority.MONITOR)
	void interact(PlayerInteractEvent e){
		DynamicLagReducer.overloadPrevention();
		return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void respawn(PlayerRespawnEvent e){
		DynamicLagReducer.overloadPrevention();
		return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void join(PlayerJoinEvent e){
		DynamicLagReducer.overloadPrevention();
		return;
	}
}
