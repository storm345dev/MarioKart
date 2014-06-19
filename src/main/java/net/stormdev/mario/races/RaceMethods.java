package net.stormdev.mario.races;

import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.utils.ParticleEffects;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class RaceMethods {
	@SuppressWarnings("unused")
	private MarioKart plugin = null;

	public RaceMethods() {
		this.plugin = MarioKart.plugin;
	}
	
	public void createExplode(final Location loc){
		Runnable run = new Runnable(){

			@Override
			public void run() {
				loc.getWorld().playSound(loc, Sound.EXPLODE, 3f, 1f);
				loc.getWorld().createExplosion(loc, 0);
				loc.getWorld().playEffect(loc, Effect.SMOKE, 3);
				ParticleEffects.sendToLocation(ParticleEffects.EXPLODE, loc, 0, 0, 0, 1, 5);
				ParticleEffects.sendToLocation(ParticleEffects.HUGE_EXPLODE, loc, 0, 0, 0, 1, 5);
				ParticleEffects.sendToLocation(ParticleEffects.LAVA_SPARK, loc, 0, 0, 0, 1, 10);
				ParticleEffects.sendToLocation(ParticleEffects.FIRE, loc, 0, 0, 0, 1, 5);
				ParticleEffects.sendToLocation(ParticleEffects.FIREWORK_SPARK, loc, 0, 0, 0, 1, 5);
				return;
			}};
		if(Bukkit.isPrimaryThread()){
			run.run();
		}
		else {
			Bukkit.getScheduler().runTask(MarioKart.plugin, run);
		}
	}
	
	public void createExplode(final Location loc, final int size){
		Runnable run = new Runnable(){

			@Override
			public void run() {
				loc.getWorld().playSound(loc, Sound.EXPLODE, 1f, 1f);
				//loc.getWorld().createExplosion(loc, 0);
				loc.getWorld().playEffect(loc, Effect.SMOKE, size);
				ParticleEffects.sendToLocation(ParticleEffects.EXPLODE, loc, 0, 0, 0, 1, size);
				ParticleEffects.sendToLocation(ParticleEffects.HUGE_EXPLODE, loc, 0, 0, 0, 1, size);
				ParticleEffects.sendToLocation(ParticleEffects.LAVA_SPARK, loc, 0, 0, 0, 1, size*2);
				ParticleEffects.sendToLocation(ParticleEffects.FIRE, loc, 0, 0, 0, 1, size);
				ParticleEffects.sendToLocation(ParticleEffects.FIREWORK_SPARK, loc, 0, 0, 0, 1, size);
				return;
			}};
		
		
		if(Bukkit.isPrimaryThread()){
			run.run();
		}
		else {
			Bukkit.getScheduler().runTask(MarioKart.plugin, run);
		}
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
