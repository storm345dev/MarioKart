package net.stormdev.mario.powerups;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.stormdev.mario.items.ItemStacks;
import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceExecutor;
import net.stormdev.mario.sound.MarioKartSound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class GreenShellPowerup extends ShellPowerup {
	private Vector direction = null;
	private BukkitTask task = null;
	
	public GreenShellPowerup(){
		super.setItemStack(getBaseItem());
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}
	
	public static PowerupType getPowerupType(){
		return PowerupType.GREEN_SHELL;
	}
	
	private static final ItemStack getBaseItem(){
		String id = MarioKart.config.getString("mariokart.greenShell");
		ItemStack i = ItemStacks.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Slows down the victim");
		lore.add("*Left click to throw forwards");
		lore.add("*Right click to throw backwards");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(MarioKart.colors.getInfo()+"Green shell");
		i.setItemMeta(im);
		
		return i;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		inHand.setAmount(inHand.getAmount() - 1);
		if(inHand.getAmount() <= 0){
			inHand.setType(Material.AIR);
		}
		player.setItemInHand(inHand);
		player.updateInventory();
		Location loc = player.getLocation();
		
		spawn(loc, player);
		
		Vector direction = player.getEyeLocation()
				.getDirection(); //The direction to fire the shell
		double speed = 0.9; //The speed to fire it at
		Boolean ux = true; //If abs.x(True) or abs.z(False) is smaller
		double x = direction.getX();
		double z = direction.getZ();
		double px = Math.abs(x);  //Make negatives become positive
		double pz = Math.abs(z);
		if (px > pz) {
			ux = false; //Set ux according to sizes
		}

		if (ux) {
			// x is smaller
			// long mult = (long) (pz/speed); - Calculate Multiplier
			x = (x / pz) * speed;
			z = (z / pz) * speed;
		} else {
			// z is smaller
			// long mult = (long) (px/speed);
			x = (x / px) * speed;
			z = (z / px) * speed;
		}
		final double fx = x;
		final double fz = z;
		this.direction = new Vector(-fx, 0, -fz);
		start();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void doLeftClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand){
		inHand.setAmount(inHand.getAmount() - 1);
		if(inHand.getAmount() <= 0){
			inHand.setType(Material.AIR);
		}
		player.setItemInHand(inHand);
		player.updateInventory();
		Location loc = player.getLocation().add(
				player.getLocation().getDirection().setY(0)
						.multiply(4));
		
		spawn(loc, player);
		
		Vector direction = player.getEyeLocation()
				.getDirection(); //The direction to fire the shell
		double speed = 1.2; //The speed to fire it at
		Boolean ux = true; //If abs.x(True) or abs.z(False) is smaller
		double x = direction.getX();
		double z = direction.getZ();
		double px = Math.abs(x);  //Make negatives become positive
		double pz = Math.abs(z);
		if (px > pz) {
			ux = false; //Set ux according to sizes
		}

		if (ux) {
			// x is smaller
			// long mult = (long) (pz/speed); - Calculate Multiplier
			x = (x / pz) * speed;
			z = (z / pz) * speed;
		} else {
			// z is smaller
			// long mult = (long) (px/speed);
			x = (x / px) * speed;
			z = (z / px) * speed;
		}
		final double fx = x;
		final double fz = z;
		this.direction = new Vector(fx, 0, fz);
		start();
	}

	@Override
	public PowerupType getType() {
		return PowerupType.GREEN_SHELL;
	}

	@Override
	public void move() {
		if(!isFired()){
			return;
		}
		Item item = getFiredItem();
		
		item.setTicksLived(1);
		item.setPickupDelay(Integer.MAX_VALUE);
		
		Location loc = item.getLocation();
		Block toHit = loc.add(direction).getBlock();
		
		if(!toHit.isEmpty() && !toHit.isLiquid()){
			item.setVelocity(direction.clone().multiply(-1));
		}
		else {
			item.setVelocity(direction);
		}
	}

	@Override
	public void start() {
		if(direction == null){
			MarioKart.logger.info("Error: Green shell vector was null when fired!");
			return;
		}
		Player owner = Bukkit.getServer().getPlayer(getOwner());
		if(owner == null || !owner.isOnline()){
			return;
		}
		if(!isFired()){
			return;
		}
		
		super.setCooldown(3); //No cooldown for tracking shells
		super.setExpiry(50); //Expire after moving 33 times
		
		task = Bukkit.getScheduler().runTaskAsynchronously(MarioKart.plugin, new BukkitRunnable(){

			@Override
			public void run() {
				while(!remove()){
					Item item = getFiredItem();
					item.setTicksLived(1);
					item.setPickupDelay(Integer.MAX_VALUE);
					
					//Move the item
					move();
					
					List<Entity> nearby = item.getNearbyEntities(1, 2, 1);
					for(Entity e:nearby){
						if(e instanceof Player){
							collide((Player) e);
						}
					}
					
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
	public void collide(Player target) {
		if(isOwner(target.getName())){
			return;
		}
		
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
	public boolean remove(){
		if(!super.isExpired()){
			return false;
		}
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
