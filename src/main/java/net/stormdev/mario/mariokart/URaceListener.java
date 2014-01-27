package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.economy.EconomyResponse;
import net.stormdev.mario.sound.MarioKartSound;
import net.stormdev.mario.utils.DynamicLagReducer;
import net.stormdev.mario.utils.HotBarSlot;
import net.stormdev.mario.utils.ItemStackFromId;
import net.stormdev.mario.utils.MarioHotBar;
import net.stormdev.mario.utils.MarioKartRaceFinishEvent;
import net.stormdev.mario.utils.RaceQueue;
import net.stormdev.mario.utils.TrackCreator;
import net.stormdev.mario.utils.shellUpdateEvent;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EnderCrystal;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class URaceListener implements Listener {
	main plugin = null;
	private boolean fairCars = true;

	public URaceListener(main plugin) {
		this.plugin = plugin;
		fairCars = main.config.getBoolean("general.ensureEqualCarSpeed");
	}

	@SuppressWarnings("deprecation")
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
		if (ItemStackFromId.equals(main.config.getString("mariokart.banana"),
				stack.getTypeId(), stack.getDurability())) {
			if(!main.marioKart.isPlayerImmune(player)){
				main.plugin.playCustomSound(player, MarioKartSound.BANANA_HIT);
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

	@EventHandler(priority = EventPriority.MONITOR)
	void playerJoin(PlayerJoinEvent event){
		System.gc();
		return;
	}
	
	@EventHandler
	void invClick(InventoryClickEvent event) {
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

	@EventHandler
	public void onWandClickEvent(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)
				&& !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Player player = event.getPlayer();
		if (!main.trackCreators.containsKey(player.getName())) {
			return;
		}
		TrackCreator creator = main.trackCreators.get(player.getName());
		Boolean wand = false;
		@SuppressWarnings("deprecation")
		int handid = player.getItemInHand().getTypeId();
		if (handid == main.config.getInt("setup.create.wand")) {
			wand = true;
		}
		creator.set(wand);
		return;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void powerups(final ucarUpdateEvent event) {
		final Player player = event.getPlayer();
		try {
			if (plugin.raceMethods.inAGame(player, false) == null) {
				return;
			}
		} catch (Exception e) {
			return;
		}
	    main.marioKart.calculate(player, event);
		return;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void trackingShells(shellUpdateEvent event) {
		// if target is null then green shell
		final Entity shell = event.getShell();
		Location shellLoc = shell.getLocation();
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
					main.plugin.playCustomSound((Player) e, MarioKartSound.TRACKING_BLEEP);
				}
			}
			sound = 3;
			shell.removeMetadata("shell.sound", plugin);
			shell.setMetadata("shell.sound", new StatValue(sound, plugin));
		} else {
			sound--;
			shell.removeMetadata("shell.sound", plugin);
			shell.setMetadata("shell.sound", new StatValue(sound, plugin));
		}
		double speed = 1.2;
		String targetName = event.getTarget();
		if (targetName != null) {
			final Player target = plugin.getServer().getPlayer(targetName);
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
			Vector vel = new Vector(x, 0, z);
			shell.setVelocity(vel);
			if (pz < 1.1 && px < 1.1) {
				String msg = main.msgs.get("mario.hit");
				msg = msg.replaceAll(Pattern.quote("%name%"), "tracking shell");
				main.plugin.playCustomSound(target, MarioKartSound.SHELL_HIT);
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
				RaceExecutor.penalty(target, ((Minecart) cart), 4);
				shell.setMetadata("shell.destroy", new StatValue(0, plugin));
				return;
			}
			return;
		} else {
			speed = 1.5;
			Vector direction = event.direction;
			if (!event.getCooldown()) {
				if (shellLoc.getBlock().getType() != Material.AIR
						&& shellLoc.getBlock().getType() != Material.CARPET) {
					// Bounce
					direction = direction.multiply(-1);
				}
			}
			shell.setVelocity(direction);
			if (!event.getCooldown()) {
				if (shell.getNearbyEntities(2, 2, 2).size() > 0) {
					List<Entity> nearby = shell.getNearbyEntities(2, 2, 2);
					for (Entity entity : nearby) {
						if (entity instanceof Player) {
							Player pl = (Player) entity;
							if (ucars.listener.inACar(pl)) {
								String msg = main.msgs.get("mario.hit");
								msg = msg.replaceAll(Pattern.quote("%name%"),
										"green shell");
								main.plugin.playCustomSound(pl, MarioKartSound.SHELL_HIT);
								pl.sendMessage(ChatColor.RED + msg);
								Entity cart = pl.getVehicle();
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
								RaceExecutor.penalty(pl, ((Minecart) cart), 4);
								shell.setMetadata("shell.destroy",
										new StatValue(0, plugin));
							}
							return;
						}
					}
				}
			}
		}

		return;
	}

	@EventHandler
	void gameQuitting(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		main.plugin.resourcedPlayers.remove(player.getName());
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
		main.plugin.resourcedPlayers.remove(player.getName());
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
	void stayInCar(VehicleExitEvent event) {
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
	void damage(EntityDamageEvent event) {
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
	void exploder(EntityExplodeEvent event) {
		if (!main.config.getBoolean("mariokart.enable")) {
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

	@EventHandler
	void signClicker(final PlayerInteractEvent event) {
		main.marioKart.calculate(event.getPlayer(), event);
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (!(event.getClickedBlock().getState() instanceof Sign)) {
			return;
		}
		final Sign sign = (Sign) event.getClickedBlock().getState();
		String[] lines = sign.getLines();
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			@Override
			public void run() {
				if(plugin.signManager.isQueueSign(sign)){
					String trackName = ChatColor.stripColor(sign.getLine(0));
					main.cmdExecutor.urace(event.getPlayer(), new String[] {
						"join", trackName, "auto" },
						event.getPlayer());
				}
				return;
			}});
		if (!ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")) {
			return;
		}
		String cmd = ChatColor.stripColor(lines[1]);
		if (cmd.equalsIgnoreCase("list")) {
			int page = 1;
			try {
				page = Integer.parseInt(ChatColor.stripColor(lines[2]));
			} catch (NumberFormatException e) {
			}
			main.cmdExecutor.urace(event.getPlayer(), new String[] { "list",
					"" + page }, event.getPlayer());
		} else if (cmd.equalsIgnoreCase("leave")
				|| cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) {
			main.cmdExecutor.urace(event.getPlayer(), new String[] { "leave" },
					event.getPlayer());
		} else if (cmd.equalsIgnoreCase("join")) {
			String mode = ChatColor.stripColor(lines[3]);
			if (mode.length() > 0) {
				main.cmdExecutor.urace(event.getPlayer(), new String[] {
						"join", ChatColor.stripColor(lines[2]).toLowerCase(),
						mode }, event.getPlayer());
			} else {
				main.cmdExecutor.urace(event.getPlayer(), new String[] {
						"join", ChatColor.stripColor(lines[2]).toLowerCase() },
						event.getPlayer());
			}
		} else if (cmd.equalsIgnoreCase("shop")) {
			main.cmdExecutor.urace(event.getPlayer(), new String[] { "shop" },
					event.getPlayer());
		}
		return;
	}

	@EventHandler
	void signWriter(SignChangeEvent event) {
		String[] lines = event.getLines();
		if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")) {
			lines[0] = main.colors.getTitle() + "[MarioKart]";
			Boolean text = true;
			String cmd = ChatColor.stripColor(lines[1]);
			if (cmd.equalsIgnoreCase("list")) {
				lines[1] = main.colors.getInfo() + "List";
				if (!(lines[2].length() < 1)) {
					text = false;
				}
				lines[2] = main.colors.getSuccess()
						+ ChatColor.stripColor(lines[2]);
			} else if (cmd.equalsIgnoreCase("join")) {
				lines[1] = main.colors.getInfo() + "Join";
				lines[2] = main.colors.getSuccess()
						+ ChatColor.stripColor(lines[2]);
				if (lines[2].equalsIgnoreCase("auto")) {
					lines[2] = main.colors.getTp() + "Auto";
				}
				lines[3] = main.colors.getInfo() + lines[3];
				text = false;
			} else if (cmd.equalsIgnoreCase("shop")) {
				lines[1] = main.colors.getInfo() + "Shop";

			} else if (cmd.equalsIgnoreCase("leave")
					|| cmd.equalsIgnoreCase("exit")
					|| cmd.equalsIgnoreCase("quit")) {
				char[] raw = cmd.toCharArray();
				if (raw.length > 1) {
					String start = "" + raw[0];
					start = start.toUpperCase();
					String body = "";
					for (int i = 1; i < raw.length; i++) {
						body = body + raw[i];
					}
					body = body.toLowerCase();
					cmd = start + body;
				}
				lines[1] = main.colors.getInfo() + cmd;
			} else if (cmd.equalsIgnoreCase("items")) {
				Location above = event.getBlock().getLocation().add(0, 1.4, 0);
				EnderCrystal crystal = (EnderCrystal) above.getWorld()
						.spawnEntity(above, EntityType.ENDER_CRYSTAL);
				above.getBlock().setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.WEST)
						.setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.NORTH)
						.setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.NORTH_WEST)
						.setType(Material.COAL_BLOCK);
				crystal.setFireTicks(0);
				crystal.setMetadata("race.pickup", new StatValue(true, plugin));
				text = false;
			} else if(cmd.equalsIgnoreCase("queues")){ 
				String track = ChatColor.stripColor(lines[2]);
				if(track.length() < 1){
					return; //No track
				}
				track = plugin.signManager.getCorrectName(track);
				if(!plugin.trackManager.raceTrackExists(track)){
					event.getPlayer().sendMessage(main.colors.getSuccess()+main.msgs.get("setup.fail.queueSign"));
					return;
				}
				//Register sign
				plugin.signManager.addQueueSign(track, event.getBlock().getLocation());
				//Tell the player it was registered successfully
				event.getPlayer().sendMessage(main.colors.getSuccess()+main.msgs.get("setup.create.queueSign"));
				final String t = track;
				main.plugin.getServer().getScheduler().runTaskLater(plugin, new BukkitRunnable(){

					@Override
					public void run() {
						plugin.signManager.updateSigns(t);
						return;
					}}, 2l);
				
				text = false;
			} else {
				text = false;
			}
			if (text) {
				lines[2] = ChatColor.ITALIC + "Right click";
				lines[3] = ChatColor.ITALIC + "to use";
			}
		}
	}

	@EventHandler
	void crystalExplode(EntityExplodeEvent event) {
		if (!(event.getEntity() instanceof EnderCrystal)) {
			return;
		}
		Entity crystal = event.getEntity();
		// if(crystal.hasMetadata("race.pickup")){
		event.setCancelled(true);
		event.setYield(0);
		Location newL = crystal.getLocation();
		Location signLoc = null;
		if ((newL.add(0, -2.4, 0).getBlock().getState() instanceof Sign)) {
			signLoc = newL.add(0, -2.4, 0);
		} else {
			return; // Let them destroy it
		}
		Location above = signLoc.add(0, 3.8, 0);
		EnderCrystal newC = (EnderCrystal) above.getWorld().spawnEntity(above,
				EntityType.ENDER_CRYSTAL);
		above.getBlock().setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.WEST)
				.setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.NORTH)
				.setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.NORTH_WEST)
				.setType(Material.COAL_BLOCK);
		newC.setFireTicks(0);
		newC.setMetadata("race.pickup", new StatValue(true, plugin));
		// }

		return;
	}

	public void spawnItemPickupBox(Location previous, Boolean force) {
		Location newL = previous;
		newL.getChunk(); // Load chunk
		Location signLoc = null;
		if ((newL.add(0, -2.4, 0).getBlock().getState() instanceof Sign)
				|| force) {
			signLoc = newL.add(0, -2.4, 0);
		} else {
			if (force) {
				double ll = newL.getY();
				Boolean foundSign = false;
				Boolean cancel = false;
				while (!foundSign && !cancel) {
					if (ll < newL.getY() - 4) {
						cancel = true;
					}
					Location i = new Location(newL.getWorld(), newL.getX(), ll,
							newL.getZ());
					if (i.getBlock().getState() instanceof Sign) {
						foundSign = true;
						signLoc = i;
					}
				}
				if (!foundSign) {
					return; // Let is be destroyed
				}
			} else {
				return; // Let them destroy it
			}
		}
		Location above = signLoc.add(0, 3.8, 0);
		EnderCrystal newC = (EnderCrystal) above.getWorld().spawnEntity(above,
				EntityType.ENDER_CRYSTAL);
		above.getBlock().setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.WEST)
				.setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.NORTH)
				.setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.NORTH_WEST)
				.setType(Material.COAL_BLOCK);
		newC.setFireTicks(0);
		newC.setMetadata("race.pickup", new StatValue(true, plugin));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	void stopCrystalFire(BlockIgniteEvent event) {
		if (event.getCause() != IgniteCause.ENDER_CRYSTAL) {
			return;
		}
		event.setCancelled(true);
		return;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void playerFireProtection(EntityDamageEvent event) {
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
	void carDeath(VehicleDamageEvent event) {
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
		if (!main.config.getBoolean("mariokart.enable")) {
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
		return;
	}

	@EventHandler
	void playerDeathEvent(PlayerDeathEvent event) {
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
	void playerRespawnEvent(PlayerRespawnEvent event) {
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
			race.updateUser(user);
		} catch (Exception e) {
		}
		Location loc = race.getTrack().getCheckpoints().get(checkpoint)
				.getLocation(plugin.getServer()).clone().add(0, 2, 0);
		Chunk chunk = loc.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
		}
		event.setRespawnLocation(loc);
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

	@EventHandler(priority = EventPriority.HIGHEST)
	void queueRespawns(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		RaceQueue r = main.plugin.raceMethods.inGameQue(player);
		if (r == null) {
			return;
		}
		event.setRespawnLocation(r.getTrack().getLobby(main.plugin.getServer()));
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	void postRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		if (plugin.raceMethods.inAGame(player, true) == null) {
			return;
		}
		Race race = plugin.raceMethods.inAGame(player, false);
		User u = race.updateUser(player);
		int checkpoint = u.getCheckpoint();
		race.updateUser(u);
		Location loc = race.getTrack().getCheckpoints().get(checkpoint)
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
		cart.setMetadata("kart.racing", new StatValue(null, main.plugin));
		cart.setPassenger(player);
		if(fairCars){
			uCarsAPI.getAPI().setUseRaceControls(cart.getUniqueId(), plugin);
		}
		player.setMetadata("car.stayIn", new StatValue(null, plugin));
		plugin.hotBarManager.updateHotBar(player);
		player.updateInventory();
		player.setScoreboard(race.board);
		main.plugin.raceScheduler.updateRace(race);
		return;
	}

	@EventHandler
	void blockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		event.setCancelled(true);
		return;
	}

	@EventHandler
	void blockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		event.setCancelled(true);
		return;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void speedo(VehicleUpdateEvent event) {
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
	void raceFinish(MarioKartRaceFinishEvent event) {
		Player player = event.getPlayer();
		main.plugin.hotBarManager.clearHotBar(player.getName());
		if (!main.config.getBoolean("general.race.rewards.enable")) {
			return;
		}
		int pos = event.getFinishPosition();
		double reward = 0;
		switch (pos) {
		case 1: {
			reward = main.config.getDouble("general.race.rewards.win");
			break;
		}
		case 2: {
			reward = main.config.getDouble("general.race.rewards.second");
			break;
		}
		case 3: {
			reward = main.config.getDouble("general.race.rewards.third");
			break;
		}
		default:
			return;
		}
		if (reward <= 0) {
			return;
		}
		if (!main.vault || main.economy == null) {
			plugin.setupEconomy(); // Economy plugin loaded after MarioKart
			if (!main.vault || main.economy == null) { // No Economy plugin
														// installed
				return;
			}
		}
		EconomyResponse r = main.economy
				.depositPlayer(player.getName(), reward);
		double b = r.balance;
		String currency = main.config
				.getString("general.race.rewards.currency");
		String msg = main.msgs.get("race.end.rewards");
		msg = msg.replaceAll(Pattern.quote("%amount%"),
				Matcher.quoteReplacement("" + reward));
		msg = msg.replaceAll(Pattern.quote("%balance%"),
				Matcher.quoteReplacement("" + b));
		msg = msg.replaceAll(Pattern.quote("%currency%"),
				Matcher.quoteReplacement("" + currency));
		msg = msg.replaceAll(Pattern.quote("%position%"), Matcher
				.quoteReplacement("" + event.getPlayerFriendlyPosition()));
		player.sendMessage(main.colors.getInfo() + msg);
		return;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void pvp(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player
				&& main.plugin.raceMethods.inAGame(
						((Player) event.getEntity()), false) != null
				&& event.getCause() == DamageCause.ENTITY_ATTACK) {
			event.setDamage(0);
			event.setCancelled(true);
		}
		return;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void pvp(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player
				&& main.plugin.raceMethods.inAGame(
						((Player) event.getEntity()), false) != null) {
			event.setDamage(0);
			event.setCancelled(true);
		}
		return;
	}

	@EventHandler
	public void hotBarScrolling(VehicleUpdateEvent event) {
		Vehicle car = event.getVehicle();
		Entity e = car.getPassenger();
		if(event instanceof ucarUpdateEvent){
			e = ((ucarUpdateEvent) event).getPlayer();
		}
		else{
			while(e!=null && !(e instanceof Player) && e.getPassenger() != null){
				e = e.getPassenger();
			}
			if(!(e instanceof Player)){
				return;
			}
		}
		final Player player = (Player) e;
		if (main.plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		if (car.hasMetadata("car.braking")
				&& !player.hasMetadata("mariokart.slotChanging")
				&& (player.getInventory().getHeldItemSlot() == 6 || player
						.getInventory().getHeldItemSlot() == 7)) {
			MarioHotBar hotBar = main.plugin.hotBarManager.getHotBar(player
					.getName());
			if (player.getInventory().getHeldItemSlot() == 6) {
				hotBar.scroll(HotBarSlot.SCROLLER);
			} else {
				hotBar.scroll(HotBarSlot.UTIL);
			}
			player.setMetadata("mariokart.slotChanging", new StatValue(true,
					main.plugin));
			main.plugin.getServer().getScheduler()
					.runTaskLater(main.plugin, new Runnable() {

						@Override
						public void run() {
							player.removeMetadata("mariokart.slotChanging",
									main.plugin);
						}
					}, 15);
			plugin.hotBarManager.updateHotBar(player);
		}
		return;
	}
}
