package net.stormdev.mario.powerups;

import java.util.List;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceExecutor;
import net.stormdev.mario.sound.MarioKartSound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.useful.ucarsCommon.StatValue;

public abstract class TrackingShellPowerup extends ShellPowerup implements TrackingShell {
	private String target;
	private BukkitTask task = null;
	
	@Override
	public void doLeftClickAction(User user, Player player, Minecart car, 
			Location carLoc, Race race, ItemStack inHand){
		return; //Don't do anything
	}
	
	@Override
	public void move() {
		if(!isFired()){
			return;
		}
		Item shell = super.getFiredItem();
		int sound = 0;
		if (shell.hasMetadata("shell.sound")) {
			sound = (Integer) ((StatValue) shell.getMetadata("shell.sound")
					.get(0)).getValue();
		}
		if (sound < 1) {
			//Shell Tracking sound
			List<Entity> nearby = shell.getNearbyEntities(15, 5, 15);
			for(Entity e:nearby){
				if(e instanceof Player){
					MarioKart.plugin.musicManager.playCustomSound((Player) e, MarioKartSound.TRACKING_BLEEP);
				}
			}
			sound = 3;
			shell.removeMetadata("shell.sound", MarioKart.plugin);
			shell.setMetadata("shell.sound", new StatValue(sound, MarioKart.plugin));
		} else {
			sound--;
			shell.removeMetadata("shell.sound", MarioKart.plugin);
			shell.setMetadata("shell.sound", new StatValue(sound, MarioKart.plugin));
		}
		
		Vector v = calculateVelocity();
		shell.setVelocity(v); //Move the shell
		
	}

	@Override
	public void collide(Player target) {
		String msg = MarioKart.msgs.get("mario.hit");
		msg = msg.replaceAll(Pattern.quote("%name%"), "tracking shell");
		MarioKart.plugin.musicManager.playCustomSound(target, MarioKartSound.SHELL_HIT);
		target.sendMessage(ChatColor.RED + msg);
		Entity cart = target.getVehicle();
		if(cart == null){
			return;
		}
		if(!(cart instanceof Minecart)){
			while(!(cart instanceof Minecart) && cart.getVehicle() != null){
				cart = cart.getVehicle();
			}
			if(!(cart instanceof Minecart)){
				return;
			}
		}
		
		MarioKart.plugin.raceMethods.createExplode(cart.getLocation(), 1);
		
		RaceExecutor.penalty(target, ((Minecart) cart), 4);
		setExpiry(0);
		return;
	}

	@Override
	public void setTarget(String player) {
		this.target = player;
	}

	@Override
	public String getTarget() {
		return this.target;
	}

	@Override
	public Vector calculateVelocity() {
		Location shellLoc = getFiredItem().getLocation();
		double speed = 1.2;
		final Player target = MarioKart.plugin.getServer().getPlayer(getTarget());
		Location targetLoc = target.getLocation();
		double x = targetLoc.getX() - shellLoc.getX();
		double z = targetLoc.getZ() - shellLoc.getZ();
		Boolean ux = true;
		double px = Math.abs(x);
		double pz = Math.abs(z);
		if (px > pz) {
			ux = false;
		}

		if (ux) {
			// x is smaller
			// long mult = (long) (pz/speed);
			x = (x / pz) * speed;
			z = (z / pz) * speed;
		} else {
			// z is smaller
			// long mult = (long) (px/speed);
			x = (x / px) * speed;
			z = (z / px) * speed;
		}
		if (pz < 1.1 && px < 1.1) {
			collide(target);
		}
		Vector vel = new Vector(x, 0, z);
		return vel;
	}

	@Override
	public void start() {
		if(!isFired()){
			return;
		}
		
		super.setCooldown(0); //No cooldown for tracking shells
		super.setExpiry(33); //Expire after moving 33 times
		
		task = Bukkit.getScheduler().runTaskAsynchronously(MarioKart.plugin, new BukkitRunnable(){

			@Override
			public void run() {
				while(!remove()){
					Item item = getFiredItem();
					item.setTicksLived(1);
					item.setPickupDelay(Integer.MAX_VALUE);
					
					//Move the item
					move();
					
					//Decrease the cooldown and expiry
					decrementCooldown();
					decrementExpiry();
					
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						return;
					}
				}
				return;
			}});
	}
	
	@Override
	public boolean remove(){
		if(!super.isExpired()){
			return false;
		}
		target = null;
		if(super.item != null)
			super.item.remove();
		super.item = null;
		super.owner = null;
		if(task != null){
			task.cancel();
		}
		return true;
	}

}
