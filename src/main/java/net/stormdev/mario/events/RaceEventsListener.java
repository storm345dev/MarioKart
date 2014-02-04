package net.stormdev.mario.events;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.economy.EconomyResponse;
import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.User;
import net.stormdev.mario.powerups.BananaPowerup;
import net.stormdev.mario.races.MarioKartRaceFinishEvent;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceExecutor;
import net.stormdev.mario.sound.MarioKartSound;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class RaceEventsListener implements Listener {
	private MarioKart plugin;
	private boolean fairCars = true;
	
	public RaceEventsListener(MarioKart plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		fairCars = MarioKart.config.getBoolean("general.ensureEqualCarSpeed");
	}
	
	@EventHandler
	void bananas(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		ItemStack stack = item.getItemStack();
		Player player = event.getPlayer();
		if (!ucars.listener.inACar(player)) {
			return;
		}
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		if (BananaPowerup.isItemSimilar(stack)) {
			if(!MarioKart.powerupManager.isPlayerImmune(player)){
				MarioKart.plugin.musicManager.playCustomSound(player, MarioKartSound.BANANA_HIT);
				item.remove();
				RaceExecutor.penalty(player, ((Minecart) player.getVehicle()), 1);
			}
			event.setCancelled(true);
			return;
		}
		return;
	}
	
	@EventHandler
	void playerDeath(PlayerDeathEvent event) {
		Race r = plugin.raceMethods.inAGame(event.getEntity(), false);
		if (r == null) {
			return;
		}
		// r.broadcast(ChatColor.GREEN + event.getEntity().getName() +
		// " respawned");
		event.setDeathMessage("");
		event.getDrops().clear();
		return;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	void vehDestroy(VehicleDamageEvent event) { // Stops player's cars being
		// broken in a race.
		Vehicle veh = event.getVehicle();
		if (veh.getPassenger() == null) {
			return;
		}
		Entity e = veh.getPassenger();
		if (!(e instanceof Player)) {
			while(!(e instanceof Player) && e.getPassenger() != null){
				e = e.getPassenger();
			}
			if(!(e instanceof Player)){
				return;
			}
		}
		Player player = (Player) e;
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		event.setCancelled(true);
		return;
	}
	
	@EventHandler
	void invClick(InventoryClickEvent event) { //Stop people moving stuff in their inventory during a race
		HumanEntity player = event.getWhoClicked();
		if (!(player instanceof Player)) {
			return;
		}
		if (!ucars.listener.inACar(player.getName())) {
			return;
		}
		if (plugin.raceMethods.inAGame((Player) player, false) == null) {
			return;
		}
		event.setCancelled(true);
		return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void powerups(final ucarUpdateEvent event) { //Tell powerup manager when a car moves (Item Boxes)
		final Player player = event.getPlayer();
		try {
			if (plugin.raceMethods.inAGame(player, false) == null) {
				return;
			}
		} catch (Exception e) {
			return;
		}
	    MarioKart.powerupManager.calculate(player, event);
		return;
	}
	
	@EventHandler
	void stayInCar(VehicleExitEvent event) { //Keep players inside their cars during a race
		Entity v = event.getVehicle();
		while(v != null && !(v instanceof Minecart)
				&& v.getVehicle() != null){
			v = v.getVehicle();
		}
		if(!(v instanceof Minecart)){
			return;
		}
		Minecart car = (Minecart) v;
		Entity e = event.getExited();
		if (!(e instanceof Player)) {
			while(e!=null && !(e instanceof Player) && e.getPassenger() != null){
				e = e.getPassenger();
			}
			if(e==null || !(e instanceof Player)){
				return;
			}
		}
		Player player = (Player) e;
		if (!(player.hasMetadata("car.stayIn"))) {
			return;
		}
		if (!ucars.listener.isACar(car)) {
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	void damage(EntityDamageEvent event) { //Block damage of cars during a race
		if (!(event.getEntityType() == EntityType.MINECART)) {
			return;
		}
		if (!(event.getCause() == DamageCause.ENTITY_EXPLOSION || event
				.getCause() == DamageCause.BLOCK_EXPLOSION)) {
			return;
		}
		if (!ucars.listener.isACar((Minecart) event.getEntity())) {
			return;
		}
		try {
			if (plugin.raceMethods.inAGame(((Player) event.getEntity()
					.getPassenger()), false) == null
					&& !(event.getEntity().hasMetadata("kart.immune"))) {
				return;
			}
		} catch (Exception e) {
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
	}
	
	@EventHandler
	void exploder(EntityExplodeEvent event) { //Stop bombs blowing up terrain
		if (!MarioKart.config.getBoolean("mariokart.enable")) {
			return;
		}
		if (event.getEntity() == null) {
			return;
		}
		if (event.getEntity().hasMetadata("explosion.none")) {
			Location loc = event.getEntity().getLocation();
			event.setCancelled(true);
			event.getEntity().remove();
			double radius = 6;
			loc.getWorld().createExplosion(loc, 0);
			Double x = (double) radius;
			Double y = (double) radius;
			Double z = (double) radius;
			List<Entity> near = event.getEntity().getNearbyEntities(x, y, z);

			Object[] entarray = near.toArray();

			Entity listent;

			for (Object s : entarray) {
				listent = (Entity) s;
				EntityType type = listent.getType();
				if (type == EntityType.MINECART) {
					if (ucars.listener.isACar((Minecart) listent)) {
						Minecart car = ((Minecart) listent);
						Entity e = car.getPassenger();
						while(e!=null && !(e instanceof Player)
								&& e.getPassenger() != null){
							e = e.getPassenger();
						}
						try {
							car.setDamage(0);
							if(e != null && e instanceof Player){
								RaceExecutor.penalty((Player) e, car, 4);
							}
							
						} catch (Exception e1) {
						}
					}
				}
			}

		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void playerProtection(EntityDamageEvent event) { //Protection during races against fire and entity attacks
		try {
			if (event.getCause() != DamageCause.FIRE
					&& event.getCause() != DamageCause.FIRE_TICK
					&& event.getCause() != DamageCause.ENTITY_ATTACK) {
				return;
			}
			if (!(event.getEntity() instanceof Player)) {
				return;
			}
			if (!ucars.listener.inACar((Player) event.getEntity())) {
				return;
			}
			if (plugin.raceMethods.inAGame(((Player) event.getEntity()), false) == null) {
				return;
			}
			Player player = ((Player) event.getEntity());
			player.addPotionEffect(new PotionEffect(
					PotionEffectType.FIRE_RESISTANCE, 2, 100));
			double health = 5;
			try {
				health = player.getHealth();
			} catch (Exception e) {
				health = Double.MAX_VALUE;
			}
			health = health + event.getDamage();
			if (health > 20) {
				health = 20;
			}
			player.setHealth(health);
			player.setFireTicks(0);
			event.setCancelled(true);
			return;
		} catch (Exception e) {
			// Fire event error - Yes it happens
			return;
		}
	}
	
	@EventHandler
	void carDamage(VehicleDamageEvent event) { //Stop vehicles getting damaged in races
		if (!(event.getVehicle() instanceof Minecart)) {
			return;
		}
		if (!ucars.listener.isACar((Minecart) event.getVehicle())) {
			return;
		}
		try {
			if (plugin.raceMethods.inAGame(((Player) event.getVehicle()
					.getPassenger()), false) == null) {
				return;
			}
		} catch (Exception e) {
			return;
		}
		if (!MarioKart.config.getBoolean("mariokart.enable")) {
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
		return;
	}
	
	@EventHandler
	void playerPreDeathEvent(PlayerDeathEvent event) { //Remove cars before respawn in races
		Player player = event.getEntity();
		Race r = plugin.raceMethods.inAGame(player, false);
		if (r == null) {
			return;
		}
		if (!(player.getVehicle() == null)) {
			Entity e = player.getVehicle();
			List<Entity> stack = new ArrayList<Entity>();
			while(e != null){
				stack.add(e);
				e = e.getVehicle();
			}
			for(Entity e1:stack){
				e1.eject();
				e1.remove();
			}
		}
		List<MetadataValue> metas = null;
		if (player.hasMetadata("car.stayIn")) {
			metas = player.getMetadata("car.stayIn");
			for (MetadataValue val : metas) {
				player.removeMetadata("car.stayIn", val.getOwningPlugin());
			}
		}
		return;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void playerRespawnEvent(PlayerRespawnEvent event) { //Handle respawns during races
		final Player player = event.getPlayer();
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		Race race = plugin.raceMethods.inAGame(player, false);
		int checkpoint = 0;
		try {
			User user = race.getUser(player);
            user.setRespawning(true);
			checkpoint = user.getCheckpoint();
			//race.updateUser(user);
		} catch (Exception e) {
		}
		Location loc = race.getTrack().getCheckpoint(checkpoint)
				.getLocation(plugin.getServer()).clone().add(0, 2, 0);
		Chunk chunk = loc.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
		}
		event.setRespawnLocation(loc);
		return;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	void postRespawn(PlayerRespawnEvent event) { //Handle post respawns in races
		final Player player = event.getPlayer();
		if (plugin.raceMethods.inAGame(player, true) == null) {
			return;
		}
		Race race = plugin.raceMethods.inAGame(player, false);
		User u = race.updateUser(player);
		int checkpoint = u.getCheckpoint();
		//race.updateUser(u);
		Location loc = race.getTrack().getCheckpoint(checkpoint)
				.getLocation(plugin.getServer()).clone().add(0, 2, 0);
		Chunk chunk = loc.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
		}
		if (player.getLocation().getChunk() != chunk) {
			Location l = new Location(chunk.getWorld(), chunk.getX(), 90,
					chunk.getZ());
			l.getChunk(); // Load the chunk
			player.teleport(l);
		}
		Minecart cart = (Minecart) loc.getWorld().spawnEntity(loc,
				EntityType.MINECART);
		cart.setMetadata("kart.racing", new StatValue(null, MarioKart.plugin));
		cart.setPassenger(player);
		if(fairCars){
			uCarsAPI.getAPI().setUseRaceControls(cart.getUniqueId(), plugin);
		}
		player.setMetadata("car.stayIn", new StatValue(null, plugin));
		plugin.hotBarManager.updateHotBar(player);
		player.updateInventory();
		player.setScoreboard(race.board);
		MarioKart.plugin.raceScheduler.updateRace(race);
		return;
	}
	
	@EventHandler
	void blockBreak(BlockBreakEvent event) { //Stop griefing during races
		Player player = event.getPlayer();
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		event.setCancelled(true);
		return;
	}

	@EventHandler
	void blockPlace(BlockPlaceEvent event) { //Stop griefing during races
		Player player = event.getPlayer();
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		event.setCancelled(true);
		return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void speedo(VehicleUpdateEvent event) { //Draw the speedo onto hotbars during races
		Entity veh = event.getVehicle();
		if (!(veh instanceof Minecart)) {
			return;
		}
		if (!ucars.listener.isACar((Minecart) veh)) {
			return;
		}
		Minecart car = (Minecart) veh;
		Entity pass = car.getPassenger();
		if (!(pass instanceof Player)) {
			while(pass != null && !(pass instanceof Player) && pass.getPassenger() != null){
				pass = pass.getPassenger();
			}
			if(!(pass instanceof Player)){
				return;
			}
		}
		Player player = (Player) pass;
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		Vector Velocity = car.getVelocity();
		double speed = (Math.abs(Velocity.getX()) + Math.abs(Velocity.getZ())) * 40;
		if (speed < 1) {
			speed = Velocity.getY();
		}
		if (speed > 100) {
			speed = 100;
		}
		player.setLevel((int) speed);
		float xpBar = (float) (speed / 100);
		if (xpBar >= 1) {
			xpBar = 0.999f;
		}
		player.setExp(xpBar);
		return;
	}
	
	@EventHandler
	void raceFinish(MarioKartRaceFinishEvent event) { //Handle rewards after players finish a race
		Player player = event.getPlayer();
		MarioKart.plugin.hotBarManager.clearHotBar(player.getName());
		if (!MarioKart.config.getBoolean("general.race.rewards.enable")) {
			return;
		}
		int pos = event.getFinishPosition();
		double reward = 0;
		switch (pos) {
		case 1: {
			reward = MarioKart.config.getDouble("general.race.rewards.win");
			break;
		}
		case 2: {
			reward = MarioKart.config.getDouble("general.race.rewards.second");
			break;
		}
		case 3: {
			reward = MarioKart.config.getDouble("general.race.rewards.third");
			break;
		}
		default:
			return;
		}
		if (reward <= 0) {
			return;
		}
		if (!MarioKart.vault || MarioKart.economy == null) {
			plugin.setupEconomy(); // Economy plugin loaded after MarioKart
			if (!MarioKart.vault || MarioKart.economy == null) { // No Economy plugin
														// installed
				return;
			}
		}
		EconomyResponse r = MarioKart.economy
				.depositPlayer(player.getName(), reward);
		double b = r.balance;
		String currency = MarioKart.config
				.getString("general.race.rewards.currency");
		String msg = MarioKart.msgs.get("race.end.rewards");
		msg = msg.replaceAll(Pattern.quote("%amount%"),
				Matcher.quoteReplacement("" + reward));
		msg = msg.replaceAll(Pattern.quote("%balance%"),
				Matcher.quoteReplacement("" + b));
		msg = msg.replaceAll(Pattern.quote("%currency%"),
				Matcher.quoteReplacement("" + currency));
		msg = msg.replaceAll(Pattern.quote("%position%"), Matcher
				.quoteReplacement("" + event.getPlayerFriendlyPosition()));
		player.sendMessage(MarioKart.colors.getInfo() + msg);
		return;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	void pvp(EntityDamageEvent event) { //Stop PVP in races
		if (event.getEntity() instanceof Player
				&& MarioKart.plugin.raceMethods.inAGame(
						((Player) event.getEntity()), false) != null
				&& event.getCause() == DamageCause.ENTITY_ATTACK) {
			event.setDamage(0);
			event.setCancelled(true);
		}
		return;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void pvp(EntityDamageByEntityEvent event) { //Stop PVP in races
		if (event.getEntity() instanceof Player
				&& MarioKart.plugin.raceMethods.inAGame(
						((Player) event.getEntity()), false) != null) {
			event.setDamage(0);
			event.setCancelled(true);
		}
		return;
	}
}
