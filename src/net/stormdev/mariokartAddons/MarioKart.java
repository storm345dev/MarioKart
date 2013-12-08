package net.stormdev.mariokartAddons;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.utils.HotBarSlot;
import net.stormdev.mario.utils.ItemStackFromId;
import net.stormdev.mario.utils.MarioHotBar;
import net.stormdev.mario.utils.MarioKartHotBarClickEvent;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.shellUpdateEvent;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.useful.ucars.ClosestFace;
import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class MarioKart {
	main plugin = null;
	private HashMap<UUID, BukkitTask> tasks = new HashMap<UUID, BukkitTask>();
	Boolean enabled = true;
	public ItemStack respawn = null;

	public MarioKart(main plugin) {
		this.plugin = plugin;
		enabled = main.config.getBoolean("mariokart.enable");
		this.respawn = new ItemStack(Material.EGG);
		ItemMeta meta = this.respawn.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Respawn");
		this.respawn.setItemMeta(meta);
	}

	@SuppressWarnings("deprecation")
	public void calculate(final Player player, Event event) {
		if (!enabled) {
			return;
		}
		if (plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		Race race = plugin.raceMethods.inAGame(player, false);
		Boolean timed = race.getType() == RaceType.TIME_TRIAL;
		// Start calculations
		if (event instanceof PlayerInteractEvent) {
			PlayerInteractEvent evt = (PlayerInteractEvent) event;
			if (!ucars.listener.inACar(evt.getPlayer())) {
				return;
			}
			if (player.hasMetadata("kart.rolling")) {
				return;
			}
			final Minecart car = (Minecart) evt.getPlayer().getVehicle();
			if ((evt.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR
					|| evt.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) && !timed) {
				ItemStack inHand = evt.getPlayer().getItemInHand();
				// If green shell, throw forward
				if (ItemStackFromId.equals(
						main.config.getString("mariokart.greenShell"),
						inHand.getTypeId(), inHand.getDurability())) {
					inHand.setAmount(inHand.getAmount() - 1);
					player.setItemInHand(inHand);
					if ((inHand.getAmount() - 1) < 1) {
						player.setItemInHand(new ItemStack(Material.AIR));
					}
					player.updateInventory();
					Location loc = player.getLocation().add(
							player.getLocation().getDirection().setY(0)
									.multiply(4));
					// Location loc =
					// player.getLocation().getBlock().getRelative(ClosestFace.getClosestFace(car.getLocation().getYaw()),
					// 4).getLocation();
					ItemStack toDrop = ItemStackFromId.get(main.config
							.getString("mariokart.greenShell"));
					final Item shell = player.getLocation().getWorld()
							.dropItem(loc, toDrop);
					// final FallingBlock shell = (FallingBlock)
					// player.getLocation().getWorld().spawnFallingBlock(loc.add(0,
					// 1.4, 0), Material.WOOL, (byte) 13);
					// shell.setPickupDelay(Integer.MAX_VALUE);
					shell.setMetadata("shell.target", new StatValue(null,
							plugin));
					shell.setMetadata("shell.cooldown", new StatValue(
							((Integer) 3), plugin));
					shell.setMetadata("shell.expiry", new StatValue(
							((Integer) 50), plugin));
					BukkitTask task = plugin.getServer().getScheduler()
							.runTaskTimer(plugin, new Runnable() {

								public void run() {
									if (shell.hasMetadata("shell.destroy")) {
										shell.remove();
										tasks.get(shell.getUniqueId()).cancel();
										tasks.remove(shell.getUniqueId());
										return;
									}
									List<MetadataValue> metas = shell
											.getMetadata("shell.expiry");
									int expiry = (Integer) ((StatValue) metas
											.get(0)).getValue();
									expiry--;
									if (expiry < 0) {
										shell.remove();
										tasks.get(shell.getUniqueId()).cancel();
										tasks.remove(shell.getUniqueId());
										return;
									}
									Boolean cool = false;
									List<MetadataValue> metas2 = shell
											.getMetadata("shell.cooldown");
									int cooldown = (Integer) ((StatValue) metas2
											.get(0)).getValue();
									if (cooldown > 0) {
										cooldown--;
										cool = true;
									}
									if (cooldown >= 0) {
										shell.removeMetadata("shell.cooldown",
												main.plugin);
										shell.setMetadata("shell.cooldown",
												new StatValue(cooldown,
														main.plugin));
									}
									shell.setTicksLived(1);
									// shell.setPickupDelay(Integer.MAX_VALUE);
									shell.removeMetadata("shell.expiry",
											main.plugin);
									shell.setMetadata("shell.expiry",
											new StatValue(expiry, main.plugin));
									Vector direction = player.getEyeLocation()
											.getDirection();
									double speed = 1.2;
									Boolean ux = true;
									double x = direction.getX();
									double z = direction.getZ();
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
									shellUpdateEvent event = new shellUpdateEvent(
											shell, null, vel, cool);
									main.plugin.getServer().getPluginManager()
											.callEvent(event);
									return;
								}
							}, 1l, 1l);
					tasks.put(shell.getUniqueId(), task);
				}
			}
			if (!(evt.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || evt
					.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {
				return;
			}
			ItemStack inHand = evt.getPlayer().getItemInHand();
			Player ply = evt.getPlayer();
			if (inHand.equals(this.respawn)) {
				if(!car.hasMetadata("car.frozen")){
					player.sendMessage(ChatColor.GREEN + "Respawning...");
					player.setHealth(0);
					evt.setCancelled(true);
				}
				return;
			}
			MarioHotBar hotBar = main.plugin.hotBarManager.getHotBar(ply.getName());
			if(hotBar.getDisplayedItem(HotBarSlot.UTIL) != null
					&& player.getInventory().getHeldItemSlot() == 7){
				MarioKartHotBarClickEvent evet = new MarioKartHotBarClickEvent(ply,
						hotBar,
						HotBarSlot.UTIL);
				main.plugin.getServer().getPluginManager().callEvent(evet);
				evt.setCancelled(true);
				return;
			}
			else if(hotBar.getDisplayedItem(HotBarSlot.SCROLLER) != null
					&& player.getInventory().getHeldItemSlot() == 6){
				MarioKartHotBarClickEvent evet = new MarioKartHotBarClickEvent(ply,
						hotBar,
						HotBarSlot.SCROLLER);
				main.plugin.getServer().getPluginManager().callEvent(evet);
				evt.setCancelled(true);
				return;
			}
			if(timed){
				return;
			}
			if (ItemStackFromId.equals(
					main.config.getString("mariokart.random"),
					inHand.getTypeId(), inHand.getDurability())) {
				inHand.setAmount(inHand.getAmount() - 1);
				ItemStack give = this.getRandomPowerup();
				if (race != null) {
					if (ply.getName().equals(race.winning)) {
						while (ItemStackFromId.equals(
								main.config.getString("mariokart.blueShell"),
								give.getTypeId(), give.getDurability())) {
							give = this.getRandomPowerup();
						}
					}
				}
				evt.getPlayer().getInventory().addItem(give);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.star"),
					inHand.getTypeId(), inHand.getDurability())) {
				inHand.setAmount(inHand.getAmount() - 1);
				car.setMetadata("kart.immune",
						new StatValue(15000, main.plugin)); // Value =
															// length(millis)
				final String pname = ply.getName();
				plugin.getServer().getScheduler()
						.runTaskLater(plugin, new Runnable() {

							public void run() {
								Player pl = main.plugin.getServer().getPlayer(
										pname);
								if (pl != null) {
									car.removeMetadata("kart.immune",
											main.plugin);
								}
							}
						}, 300l);
				plugin.getServer().getScheduler()
						.runTaskAsynchronously(plugin, new Runnable() {

							public void run() {
								int amount = 5;
								while (amount > 0) {
									if (ucars.listener.inACar(player)) {
										player.getLocation()
												.getWorld()
												.playSound(
														player.getLocation(),
														Sound.BURP, 3, 1);
									}
									try {
										Thread.sleep(3000);
									} catch (InterruptedException e) {
									}
									amount--;
								}
								return;
							}
						});
				ucars.listener.carBoost(ply.getName(), 35, 15000,
						ucars.config.getDouble("general.cars.defSpeed")); // Apply
																			// speed
																			// boost
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.mushroom"),
					inHand.getTypeId(), inHand.getDurability())) {
				inHand.setAmount(inHand.getAmount() - 1);
				ucars.listener.carBoost(ply.getName(), 19, 9000,
						ucars.config.getDouble("general.cars.defSpeed")); // Apply
																			// speed
																			// boost
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.redShell"),
					inHand.getTypeId(), inHand.getDurability())) {
				SortedMap<String, Double> sorted = race.getRaceOrder();
				Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				int ppos = 0;
				for (int i = 0; i < pls.length; i++) {
					if (pls[i].equals(player.getName())) {
						ppos = i;
					}
				}
				int tpos = ppos - 1;
				if (tpos < 0) {
					tpos = ppos + 1;
					if (tpos < 0 || tpos >= pls.length) {
						return;
					}
				}
				final String targetName = (String) pls[tpos];
				inHand.setAmount(inHand.getAmount() - 1);
				ItemStack toDrop = ItemStackFromId.get(main.config
						.getString("mariokart.redShell"));
				final Item shell = player.getLocation().getWorld()
						.dropItem(player.getLocation(), toDrop);
				// DEBUG: final Entity shell =
				// player.getLocation().getWorld().spawnEntity(player.getLocation().add(0,
				// 1.3, 0), EntityType.MINECART_CHEST);
				shell.setPickupDelay(Integer.MAX_VALUE);
				shell.setMetadata("shell.target", new StatValue(targetName,
						plugin));
				shell.setMetadata("shell.expiry", new StatValue(((Integer) 33),
						plugin));
				BukkitTask task = plugin.getServer().getScheduler()
						.runTaskTimer(plugin, new Runnable() {

							public void run() {
								if (shell.hasMetadata("shell.destroy")) {
									shell.remove();
									tasks.get(shell.getUniqueId()).cancel();
									tasks.remove(shell.getUniqueId());
									return;
								}
								List<MetadataValue> metas = shell
										.getMetadata("shell.expiry");
								int expiry = (Integer) ((StatValue) metas
										.get(0)).getValue();
								expiry--;
								if (expiry < 0) {
									shell.remove();
									tasks.get(shell.getUniqueId()).cancel();
									tasks.remove(shell.getUniqueId());
									return;
								}
								shell.setTicksLived(1);
								shell.setPickupDelay(Integer.MAX_VALUE);
								shell.removeMetadata("shell.expiry",
										main.plugin);
								shell.setMetadata("shell.expiry",
										new StatValue(expiry, main.plugin));
								shellUpdateEvent event = new shellUpdateEvent(
										shell, targetName, null, false);
								main.plugin.getServer().getPluginManager()
										.callEvent(event);
								return;
							}
						}, 3l, 3l);
				tasks.put(shell.getUniqueId(), task);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.blueShell"),
					inHand.getTypeId(), inHand.getDurability())) {
				SortedMap<String, Double> sorted = race.getRaceOrder();
				Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				final String targetName = (String) pls[0];
				inHand.setAmount(inHand.getAmount() - 1);
				ItemStack toDrop = ItemStackFromId.get(main.config
						.getString("mariokart.blueShell"));
				final Item shell = player.getLocation().getWorld()
						.dropItem(player.getLocation(), toDrop);
				// DEBUG: final Entity shell =
				// player.getLocation().getWorld().spawnEntity(player.getLocation().add(0,
				// 1.3, 0), EntityType.MINECART_CHEST);
				shell.setPickupDelay(Integer.MAX_VALUE);
				shell.setMetadata("shell.target", new StatValue(targetName,
						plugin));
				shell.setMetadata("shell.expiry", new StatValue(((Integer) 66),
						plugin));
				BukkitTask task = plugin.getServer().getScheduler()
						.runTaskTimer(plugin, new Runnable() {

							public void run() {
								if (shell.hasMetadata("shell.destroy")) {
									shell.remove();
									tasks.get(shell.getUniqueId()).cancel();
									tasks.remove(shell.getUniqueId());
									return;
								}
								List<MetadataValue> metas = shell
										.getMetadata("shell.expiry");
								int expiry = (Integer) ((StatValue) metas
										.get(0)).getValue();
								expiry--;
								if (expiry < 0) {
									shell.remove();
									tasks.get(shell.getUniqueId()).cancel();
									tasks.remove(shell.getUniqueId());
									return;
								}
								shell.setTicksLived(1);
								shell.setPickupDelay(Integer.MAX_VALUE);
								shell.removeMetadata("shell.expiry",
										main.plugin);
								shell.setMetadata("shell.expiry",
										new StatValue(expiry, main.plugin));
								shellUpdateEvent event = new shellUpdateEvent(
										shell, targetName, null, false);
								main.plugin.getServer().getPluginManager()
										.callEvent(event);
								return;
							}
						}, 3l, 3l);
				tasks.put(shell.getUniqueId(), task);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.greenShell"),
					inHand.getTypeId(), inHand.getDurability())) {
				inHand.setAmount(inHand.getAmount() - 1);
				ItemStack toDrop = ItemStackFromId.get(main.config
						.getString("mariokart.greenShell"));
				Location loc = player.getLocation().add(
						player.getLocation().getDirection().multiply(-4));
				// Location loc =
				// player.getLocation().getBlock().getRelative(ClosestFace.getClosestFace(car.getLocation().getYaw()),
				// -4).getLocation();
				final Item shell = player.getLocation().getWorld()
						.dropItem(loc, toDrop);
				// DEBUG: final Entity shell =
				// player.getLocation().getWorld().spawnEntity(player.getLocation().add(0,
				// 1.3, 0), EntityType.MINECART_CHEST);
				shell.setPickupDelay(Integer.MAX_VALUE);
				shell.setMetadata("shell.target", new StatValue(null, plugin));
				shell.setMetadata("shell.cooldown", new StatValue(
						((Integer) 2), plugin));
				shell.setMetadata("shell.expiry", new StatValue(((Integer) 50),
						plugin));
				BukkitTask task = plugin.getServer().getScheduler()
						.runTaskTimer(plugin, new Runnable() {

							public void run() {
								if (shell.hasMetadata("shell.destroy")) {
									shell.remove();
									tasks.get(shell.getUniqueId()).cancel();
									tasks.remove(shell.getUniqueId());
									return;
								}
								List<MetadataValue> metas = shell
										.getMetadata("shell.expiry");
								int expiry = (Integer) ((StatValue) metas
										.get(0)).getValue();
								expiry--;
								if (expiry < 0) {
									shell.remove();
									tasks.get(shell.getUniqueId()).cancel();
									tasks.remove(shell.getUniqueId());
									return;
								}
								Boolean cool = false;
								List<MetadataValue> metas2 = shell
										.getMetadata("shell.cooldown");
								int cooldown = (Integer) ((StatValue) metas2
										.get(0)).getValue();
								if (cooldown > 0) {
									cooldown--;
									cool = true;
								}
								shell.setTicksLived(1);
								shell.setPickupDelay(Integer.MAX_VALUE);
								shell.removeMetadata("shell.expiry",
										main.plugin);
								shell.setMetadata("shell.expiry",
										new StatValue(expiry, main.plugin));
								Vector direction = player.getEyeLocation()
										.getDirection();
								double speed = 1.2;
								Boolean ux = true;
								double x = direction.getX();
								double z = direction.getZ();
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
								Vector vel = new Vector(-x, 0, -z);
								shellUpdateEvent event = new shellUpdateEvent(
										shell, null, vel, cool);
								main.plugin.getServer().getPluginManager()
										.callEvent(event);
								return;
							}
						}, 3l, 3l);
				tasks.put(shell.getUniqueId(), task);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.bomb"),
					inHand.getTypeId(), inHand.getDurability())) {
				inHand.setAmount(inHand.getAmount() - 1);
				final Vector vel = ply.getEyeLocation().getDirection();
				final TNTPrimed tnt = (TNTPrimed) car.getLocation().getWorld()
						.spawnEntity(car.getLocation(), EntityType.PRIMED_TNT);
				tnt.setFuseTicks(80);
				tnt.setMetadata("explosion.none", new StatValue(null, plugin));
				vel.setY(0.2); // Distance to throw it
				tnt.setVelocity(vel);
				final MoveableInt count = new MoveableInt(12);
				plugin.getServer().getScheduler()
						.runTaskAsynchronously(plugin, new Runnable() {
							public void run() {
								if (count.getInt() > 0) {
									count.setInt(count.getInt() - 1);
									tnt.setVelocity(vel);
									tnt.setMetadata("explosion.none",
											new StatValue(null, plugin));
									try {
										Thread.sleep(50);
									} catch (InterruptedException e) {
									}
								} else {
									return;
								}
							}
						});
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.lightning"),
					inHand.getTypeId(), inHand.getDurability())) {
				SortedMap<String, Double> sorted = race.getRaceOrder();
				Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				int ppos = 0;
				for (int i = 0; i < pls.length; i++) {
					if (pls[i].equals(player.getName())) {
						ppos = i;
					}
				}
				double Cur = ucars.config.getDouble("general.cars.defSpeed");
				double desired = 10;
				double power = Cur - desired;
				if (power < 0) {
					power = 0;
				}
				power = -power;
				for (int i = 0; i < pls.length; i++) {
					if (ppos != i) { // If not the player toggling the lightning
						Player pl = plugin.getServer().getPlayer(
								(String) pls[i]);
						pl.getWorld().strikeLightningEffect(pl.getLocation());
						if (pl.getVehicle() != null) {
							if (pl.getVehicle() instanceof Minecart) {
								main.listener.penalty(
										(Minecart) pl.getVehicle(), 4);
								ucars.listener
										.carBoost(
												pl.getName(),
												power,
												8000,
												ucars.config
														.getDouble("general.cars.defSpeed"));
							}
						}
					}
				}
				inHand.setAmount(inHand.getAmount() - 1);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.pow"), inHand.getTypeId(),
					inHand.getDurability())) {
				SortedMap<String, Double> sorted = race.getRaceOrder();
				Set<String> keys = sorted.keySet();
				final Object[] pls = (Object[]) keys.toArray();
				int pppos = 0;
				for (int i = 0; i < pls.length; i++) {
					if (pls[i].equals(player.getName())) {
						pppos = i;
					}
				}
				final int ppos = pppos;
				plugin.getServer().getScheduler()
						.runTaskAsynchronously(plugin, new Runnable() {
							public void run() {
								int count = 3;
								while (count > 0) {
									for (int i = 0; i < pls.length && i <= ppos; i++) {
										Player pl = plugin.getServer()
												.getPlayer((String) pls[i]);
										pl.sendMessage(main.colors.getTitle()
												+ "[MarioKart:] "
												+ main.colors.getInfo() + count);
									}
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									count--;
								}
								plugin.getServer().getScheduler()
										.runTask(plugin, new Runnable() {
											public void run() {
												for (int i = 0; i < pls.length
														&& i < ppos; i++) {
													Player pl = plugin
															.getServer()
															.getPlayer(
																	(String) pls[i]);
													if (pl.getVehicle() != null) {
														if (pl.getVehicle() instanceof Minecart) {
															if (!pl.getVehicle()
																	.hasMetadata(
																			"car.braking")
																	&& !pl.getVehicle()
																			.hasMetadata(
																					"kart.immune")) {
																String msg = main.msgs
																		.get("mario.hit");
																msg = msg
																		.replaceAll(
																				Pattern.quote("%name%"),
																				"pow block");
																pl.getWorld()
																		.playSound(
																				pl.getLocation(),
																				Sound.STEP_WOOD,
																				1f,
																				0.25f);
																pl.sendMessage(ChatColor.RED
																		+ msg);
																main.listener
																		.penalty(
																				(Minecart) pl
																						.getVehicle(),
																				2);
															}
														}
													}
												}
											}
										});
							}
						});
				inHand.setAmount(inHand.getAmount() - 1);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.banana"),
					inHand.getTypeId(), inHand.getDurability())) {
				BlockFace face = ClosestFace.getClosestFace(player
						.getLocation().getYaw());
				Location loc = player.getLocation().getBlock()
						.getRelative(face, -1).getLocation();
				loc.getWorld().dropItemNaturally(
						loc,
						ItemStackFromId.get(main.config
								.getString("mariokart.banana")));
				inHand.setAmount(inHand.getAmount() - 1);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.boo"), inHand.getTypeId(),
					inHand.getDurability())) {
				PotionEffect effect = new PotionEffect(
						PotionEffectType.INVISIBILITY, 120, 10);
				SortedMap<String, Double> sorted = race.getRaceOrder();
				Set<String> keys = sorted.keySet();
				final Object[] pls = (Object[]) keys.toArray();
				int pppos = 0;
				for (int i = 0; i < pls.length; i++) {
					if (pls[i].equals(player.getName())) {
						pppos = i;
					}
				}
				int pos = pppos - 1;
				if (!(pos < 0)) {
					final Player pl = main.plugin.getServer().getPlayer(
							(String) pls[pos]);
					pl.setMetadata("kart.rolling", new StatValue(true, plugin));
					pl.getInventory().clear();
					main.listener.updateHotBar(pl);
					pl.getInventory().addItem(
							PowerupMaker.getPowerup(Powerup.BOO, 1));
					PotionEffect nausea = new PotionEffect(
							PotionEffectType.CONFUSION, 240, 10);
					pl.addPotionEffect(nausea, true);
					pl.getWorld().playSound(pl.getLocation(),
							Sound.AMBIENCE_CAVE, 1, 1);
					pl.updateInventory();
					String msg = main.msgs.get("mario.hit");
					msg = msg.replaceAll("%name%", "ghost");
					pl.sendMessage(main.colors.getInfo() + msg);
					plugin.getServer().getScheduler()
							.runTaskLater(plugin, new Runnable() {

								public void run() {
									pl.removeMetadata("kart.rolling", plugin);
									pl.getInventory().clear();
									main.listener.updateHotBar(pl);
									pl.updateInventory();
								}
							}, 240l);
				}
				player.addPotionEffect(effect, true);
				inHand.setAmount(inHand.getAmount() - 1);
			}
			evt.getPlayer().setItemInHand(inHand);
			evt.getPlayer().updateInventory(); // Fix 1.6 bug with inventory not
												// updating
		} else if (event instanceof ucarUpdateEvent) {
			ucarUpdateEvent evt = (ucarUpdateEvent) event;
			Minecart car = (Minecart) evt.getVehicle();
			Block under = car.getLocation().add(0, -1, 0).getBlock();
			main.listener.updateHotBar(player);
			if(car.hasMetadata("car.braking")
					&& !player.hasMetadata("mariokart.slotChanging")
					&& (player.getInventory().getHeldItemSlot() == 6
					|| player.getInventory().getHeldItemSlot() == 7)){
				MarioHotBar hotBar = main.plugin.hotBarManager.getHotBar(player.getName());
				if(player.getInventory().getHeldItemSlot() == 6){
					hotBar.scroll(HotBarSlot.SCROLLER);
				}
				else{
					hotBar.scroll(HotBarSlot.UTIL);
				}
				player.setMetadata("mariokart.slotChanging", new StatValue(true, main.plugin));
				main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){

					@Override
					public void run() {
						player.removeMetadata("mariokart.slotChanging", main.plugin);
					}}, 40);
			}
			if(timed){
				return;
			}
			if (under.getType() == Material.COAL_BLOCK
					|| under.getType() == Material.COAL_BLOCK
					|| under.getType() == Material.COAL_BLOCK) {
				Sign sign = null;
				Location uu = (Location) under.getRelative(BlockFace.DOWN)
						.getLocation();
				Location first = uu;
				try {
					sign = (Sign) uu.getBlock().getState();
				} catch (Exception e) {
					try {
						uu = uu.getBlock().getRelative(BlockFace.SOUTH)
								.getLocation();
						sign = (Sign) uu.getBlock().getState();
					} catch (Exception e1) {
						try {
							uu = uu.getBlock().getRelative(BlockFace.EAST)
									.getLocation();
							sign = (Sign) uu.getBlock().getState();
						} catch (Exception e2) {
							try {
								uu = uu.getBlock().getRelative(BlockFace.NORTH)
										.getLocation();
								sign = (Sign) uu.getBlock().getState();
							} catch (Exception e3) {
								try {
									uu = uu.getBlock()
											.getRelative(BlockFace.WEST)
											.getLocation();
									sign = (Sign) uu.getBlock().getState();
								} catch (Exception e4) {
									try {
										uu = uu.getBlock()
												.getRelative(BlockFace.SOUTH)
												.getLocation();
										sign = (Sign) uu.getBlock().getState();
									} catch (Exception e5) {
										try {
											uu = first
													.getBlock()
													.getRelative(
															BlockFace.NORTH)
													.getLocation();
											sign = (Sign) uu.getBlock()
													.getState();
										} catch (Exception e6) {
											try {
												uu = first
														.getBlock()
														.getRelative(
																BlockFace.EAST)
														.getLocation();
												sign = (Sign) uu.getBlock()
														.getState();
											} catch (Exception e7) {
												return;
											}
										}
									}
								}
							}
						}
					}
				}
				final String[] lines = sign.getLines();
				if (ChatColor.stripColor(lines[0]).equalsIgnoreCase(
						"[MarioKart]")
						|| ChatColor.stripColor(lines[0]).equalsIgnoreCase(
								"[uRace]")) {
					if (ChatColor.stripColor(lines[1])
							.equalsIgnoreCase("items")) {
						if (player.hasMetadata("kart.rolling")) {
							return;
						}
						final Race r = race;
						final Location signLoc = sign.getLocation();
						if (r.reloadingItemBoxes.contains(signLoc)) {
							return; // Box is reloading
						}
						/*
						 * if(ChatColor.stripColor(lines[3]).equalsIgnoreCase("wait"
						 * )){ return; }
						 */
						if (player.getInventory().getContents().length > 0) {
							player.getInventory().clear();
							main.listener.updateHotBar(player);
						}
						ItemStack give = null;
						if (ChatColor.stripColor(lines[2]).equalsIgnoreCase(
								"all")) {
							// Give all items
							ItemStack a = this.getRandomPowerup();
							ItemStack b = this.getRandomBoost();
							int randomNumber = plugin.random.nextInt(3);
							if (randomNumber < 1) {
								give = b;
							} else {
								give = a;
							}
							Player ply = ((Player) car.getPassenger());
							if (race != null) {
								if (ply.getName().equals(race.winning)) {
									while (ItemStackFromId.equals(main.config
											.getString("mariokart.blueShell"),
											give.getTypeId(), give
													.getDurability())) {
										give = this.getRandomPowerup();
									}
								}
							}
						} else {
							// Give mario items
							Player ply = ((Player) car.getPassenger());
							give = this.getRandomPowerup();
							if (race != null) {
								if (ply.getName().equals(race.winning)) {
									while (ItemStackFromId.equals(main.config
											.getString("mariokart.blueShell"),
											give.getTypeId(), give
													.getDurability())) {
										give = this.getRandomPowerup();
									}
								}
							}
						}
						final Player ply = ((Player) car.getPassenger());
						ply.setMetadata("kart.rolling", new StatValue(true,
								plugin));
						final ItemStack get = give;
						plugin.getServer().getScheduler()
								.runTaskAsynchronously(plugin, new Runnable() {

									public void run() {
										int min = 0;
										int max = 20;
										int delay = 100;
										World world = ply.getWorld();
										int z = plugin.random
												.nextInt(max - min) + min;
										for (int i = 0; i <= z; i++) {
											ply.getInventory().clear();
											main.listener.updateHotBar(player);
											ply.getInventory().addItem(
													getRandomPowerup());
											ply.updateInventory();
											world.playSound(ply.getLocation(),
													Sound.NOTE_PIANO, 0.2f,
													1.5f);
											try {
												Thread.sleep(delay);
											} catch (InterruptedException e) {
											}
											delay = delay + (z / 100 * i);
											if (delay > 1000) {
												delay = 1000;
											}
										}
										ply.getInventory().clear();
										main.listener.updateHotBar(ply);
										ply.getInventory().addItem(get);
										ply.removeMetadata("kart.rolling",
												plugin);
										ply.updateInventory();
										return;
									}
								});
						List<Entity> ents = ply.getNearbyEntities(1, 2, 1);
						r.reloadingItemBoxes.add(signLoc);
						main.plugin.raceScheduler.updateRace(r);
						Location eLoc = null;
						for (Entity ent : ents) {
							if (ent instanceof EnderCrystal) {
								eLoc = ent.getLocation();
								ent.remove();
							}
						}
						if(eLoc == null){
							//Set crystal spawn loc from signLoc
							eLoc = signLoc.clone().add(0, 2.4, 0);
						}
						final Location loc = eLoc;
						plugin.getServer().getScheduler()
						.runTaskLater(plugin, new Runnable() {

							public void run() {
								if(!r.reloadingItemBoxes.contains(signLoc)){
									return; //ItemBox has been respawned
								}
								Chunk c = loc.getChunk();
								if(c.isLoaded()){
									c.load(true);
								}
								r.reloadingItemBoxes.remove(signLoc);
								main.listener.spawnItemPickupBox(loc, true);
								main.plugin.raceScheduler.updateRace(r);
								return;
							}
						}, 200l);
					}
				}
			}
		}
		// End calculations
		return;
	}

	public ItemStack getRandomBoost() {
		int type = 1;
		int min = 0;
		Integer[] amts = new Integer[] { 1, 1, 3, 2, 2, 2, 2 };
		int max = amts.length;
		int randomNumber = plugin.random.nextInt(max - min) + min;
		type = amts[randomNumber];
		if (type == 1) {
			return ItemStackFromId.get(ucars.config
					.getString("general.cars.lowBoost"));
		} else if (type == 2) {
			return ItemStackFromId.get(ucars.config
					.getString("general.cars.medBoost"));
		}
		return ItemStackFromId.get(ucars.config
				.getString("general.cars.highBoost"));
	}

	public ItemStack getRandomPowerup() {
		Powerup[] pows = Powerup.values();
		int min = 0;
		int max = pows.length;
		int randomNumber = plugin.random.nextInt(max - min) + min;
		Powerup pow = pows[randomNumber];
		Integer[] amts = new Integer[] { 1, 1, 1, 1, 1, 1, 1, 3, 1 };
		min = 0;
		max = amts.length - 1;
		if (min < 1) {
			min = 0;
		}
		if (max < 1) {
			max = 0;
		}
		randomNumber = plugin.random.nextInt(max - min) + min;
		return PowerupMaker.getPowerup(pow, amts[randomNumber]);
	}

}
