package net.stormdev.mariokartAddons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.RaceExecutor;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.sound.MarioKartSound;
import net.stormdev.mario.utils.HotBarSlot;
import net.stormdev.mario.utils.ItemStackFromId;
import net.stormdev.mario.utils.MarioHotBar;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.shellUpdateEvent;
import net.stormdev.mariokartAddons.items.BananaPowerup;
import net.stormdev.mariokartAddons.items.BlueShellPowerup;
import net.stormdev.mariokartAddons.items.GreenShellPowerup;
import net.stormdev.mariokartAddons.items.Powerup;
import net.stormdev.mariokartAddons.items.PowerupMaker;
import net.stormdev.mariokartAddons.items.PowerupType;
import net.stormdev.mariokartAddons.items.RedShellPowerup;

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
		final Race race = plugin.raceMethods.inAGame(player, false);
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
			Entity e = evt.getPlayer().getVehicle();
			if(!(evt.getPlayer().getVehicle() instanceof Minecart)){
				while(e != null && !(e instanceof Minecart) && e.getVehicle() != null){
					e = e.getVehicle();
				}
				if(!(e instanceof Minecart)){
					return;
				}
			}
			final Minecart car = (Minecart) e;
			if ((evt.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR || evt
					.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK)
					&& !timed) {
				ItemStack inHand = evt.getPlayer().getItemInHand();
				// If green shell, throw forward
				if(GreenShellPowerup.isItemSimilar(inHand)){
					GreenShellPowerup shell = new GreenShellPowerup();
					shell.setOwner(player.getName());
					shell.doLeftClickAction(race.getUser(player), player, car, car.getLocation(), race, inHand);
				}
			}
			if (!(evt.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || evt
					.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {
				return;
			}
			final ItemStack inHand = evt.getPlayer().getItemInHand();
			Player ply = evt.getPlayer();
			if (inHand.equals(this.respawn)) {
				if (!car.hasMetadata("car.frozen")) {
					player.sendMessage(ChatColor.GREEN + "Respawning...");
					player.setHealth(0);
					evt.setCancelled(true);
				}
				return;
			}
			MarioHotBar hotBar = main.plugin.hotBarManager.getHotBar(ply
					.getName());
			if (hotBar.getDisplayedItem(HotBarSlot.UTIL) != null
					&& player.getInventory().getHeldItemSlot() == 7) {
				main.plugin.hotBarManager.executeClick(ply, hotBar, HotBarSlot.UTIL);
				evt.setCancelled(true);
				return;
			} else if (hotBar.getDisplayedItem(HotBarSlot.SCROLLER) != null
					&& player.getInventory().getHeldItemSlot() == 6) {
				main.plugin.hotBarManager.executeClick(ply, hotBar, HotBarSlot.SCROLLER);
				evt.setCancelled(true);
				return;
			}
			if (timed) {
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
				ply.setMetadata("kart.immune",
						new StatValue(15000, main.plugin));
				final String pname = ply.getName();
				plugin.getServer().getScheduler()
						.runTaskLater(plugin, new Runnable() {

							@Override
							public void run() {
								Player pl = main.plugin.getServer().getPlayer(
										pname);
								if (pl != null) {
									pl.removeMetadata("kart.immune", main.plugin);
									car.removeMetadata("kart.immune",
											main.plugin);
								}
							}
						}, 300l);
				plugin.getServer().getScheduler()
						.runTaskAsynchronously(plugin, new Runnable() {

							@Override
							public void run() {
								int amount = 5;
								while (amount > 0) {
									if (ucars.listener.inACar(player)) {
										if(!plugin.playCustomSound(player, MarioKartSound.STAR_RIFF)){
											player.getLocation()
											.getWorld()
											.playSound(
													player.getLocation(),
													Sound.BURP, 3, 1);
										}
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
			} else if(RedShellPowerup.isItemSimilar(inHand)){
				RedShellPowerup powerup = new RedShellPowerup();
				powerup.setOwner(player.getName());
				powerup.doRightClickAction(race.getUser(player), player, car, car.getLocation(), race, inHand);
			}
			else if(BlueShellPowerup.isItemSimilar(inHand)){
				BlueShellPowerup powerup = new BlueShellPowerup();
				powerup.setOwner(player.getName());
				powerup.doRightClickAction(race.getUser(player), player, car, car.getLocation(), race, inHand);
			}
			else if(GreenShellPowerup.isItemSimilar(inHand)){
				GreenShellPowerup powerup = new GreenShellPowerup();
				powerup.setOwner(player.getName());
				powerup.doRightClickAction(race.getUser(player), player, car, car.getLocation(), race, inHand);
			}
			else if (ItemStackFromId.equals(
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
							@Override
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
				double Cur = ucars.config.getDouble("general.cars.defSpeed");
				double desired = 10;
				double power = Cur - desired;
				if (power < 0) {
					power = 0;
				}
				power = -power;
				for (String name:keys) {
					Player pla = plugin.getServer().getPlayer(
							name);
					if(!name.equals(player.getName())
							&& !isPlayerImmune(pla)){
						Entity c = pla.getVehicle();
						while(c!=null && !(c instanceof Minecart) && c.getVehicle() != null){
							c = c.getVehicle();
						}
						if(!(c instanceof Minecart)){
							c = null;
						}
						Minecart cart = (Minecart) c;
						pla.getWorld().strikeLightningEffect(pla.getLocation());
						if(cart != null){
							RaceExecutor.penalty(pla,
									cart, (long) 1.5);
						}
						ucars.listener
								.carBoost(
										pla.getName(),
										power,
										8000,
										ucars.config
												.getDouble("general.cars.defSpeed"));
					}
			    }
				inHand.setAmount(inHand.getAmount() - 1);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.pow"), inHand.getTypeId(),
					inHand.getDurability())) {
				SortedMap<String, Double> sorted = race.getRaceOrder();
				Set<String> keys = sorted.keySet();
				final Object[] pls = keys.toArray();
				int pppos = 0;
				for (int i = 0; i < pls.length; i++) {
					if (pls[i].equals(player.getName())) {
						pppos = i;
					}
				}
				final int ppos = pppos;
				plugin.getServer().getScheduler()
						.runTaskAsynchronously(plugin, new Runnable() {
							@Override
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
											@Override
											public void run() {
												for (int i = 0; i < pls.length
														&& i < ppos; i++) {
													Player pl = plugin
															.getServer()
															.getPlayer(
																	(String) pls[i]);
													Entity e = pl.getVehicle();
													while(e!=null && !(e instanceof Minecart) && e.getVehicle() != null){
														e = e.getVehicle();
													}
													if(e == null || !(e instanceof Minecart)){
														return;
													}
													Minecart cart = (Minecart) e;
													if (!cart
															.hasMetadata(
																	"car.braking")
															&& !isCarImmune(cart)) {
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
														
														RaceExecutor.penalty(pl, 
																		cart,
																		2);
													}
												}
												return;
											}
										});
							}
						});
				inHand.setAmount(inHand.getAmount() - 1);
			} else if (BananaPowerup.isItemSimilar(inHand)) {
				BananaPowerup powerup = new BananaPowerup();
				powerup.setOwner(player.getName());
				powerup.doRightClickAction(race.getUser(player), player, car, car.getLocation(), race, inHand);
			} else if (ItemStackFromId.equals(
					main.config.getString("mariokart.boo"), inHand.getTypeId(),
					inHand.getDurability())) {
				main.plugin.getServer().getScheduler().runTask(main.plugin, new Runnable(){

					@Override
					public void run() {
						PotionEffect effect = new PotionEffect(
								PotionEffectType.INVISIBILITY, 120, 10);
						SortedMap<String, Double> sorted = race.getRaceOrder();
						Set<String> keys = sorted.keySet();
						final Object[] pls = keys.toArray();
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
							if(!isPlayerImmune(pl)){
								pl.setMetadata("kart.rolling", new StatValue(true, plugin));
								pl.getInventory().clear();
								main.plugin.hotBarManager.updateHotBar(pl);
								pl.getInventory().addItem(
										PowerupMaker.getPowerup(PowerupType.BOO, 1));
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

											@Override
											public void run() {
												pl.removeMetadata("kart.rolling", plugin);
												pl.getInventory().clear();
												main.plugin.hotBarManager.updateHotBar(pl);
												pl.updateInventory();
											}
										}, 240l);
							}
						}
						player.addPotionEffect(effect, true);
						return;
					}});
				inHand.setAmount(inHand.getAmount() - 1);
			}
			evt.getPlayer().setItemInHand(inHand);
			evt.getPlayer().updateInventory(); // Fix 1.6 bug with inventory not
												// updating
		} else if (event instanceof ucarUpdateEvent) {
			ucarUpdateEvent evt = (ucarUpdateEvent) event;
			Minecart car = (Minecart) evt.getVehicle();
			Block under = car.getLocation().add(0, -1, 0).getBlock();
			if (timed) {
				return;
			}
			if (under.getType() == Material.COAL_BLOCK) {
				Sign sign = null;
				Location uu = under.getRelative(BlockFace.DOWN)
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
							main.plugin.hotBarManager.updateHotBar(player);
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
							Player ply = evt.getPlayer();
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
							Player ply = evt.getPlayer();
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
						final Player ply = evt.getPlayer();
						ply.setMetadata("kart.rolling", new StatValue(true,
								plugin));
						final ItemStack get = give;
						plugin.getServer().getScheduler()
								.runTaskAsynchronously(plugin, new Runnable() {

									@Override
									public void run() {
										int min = 0;
										int max = 20;
										int delay = 100;
										World world = ply.getWorld();
										int z = plugin.random
												.nextInt(max - min) + min;
										for (int i = 0; i <= z; i++) {
											ply.getInventory().clear();
											main.plugin.hotBarManager.updateHotBar(player);
											ply.getInventory().addItem(
													getRandomPowerup());
											ply.updateInventory();
											main.plugin.playCustomSound(ply, MarioKartSound.ITEM_SELECT_BEEP);
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
										main.plugin.hotBarManager.updateHotBar(ply);
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
						if (eLoc == null) {
							// Set crystal spawn loc from signLoc
							eLoc = signLoc.clone().add(0, 2.4, 0);
						}
						final Location loc = eLoc;
						plugin.getServer().getScheduler()
								.runTaskLater(plugin, new Runnable() {

									@Override
									public void run() {
										if (!r.reloadingItemBoxes
												.contains(signLoc)) {
											return; // ItemBox has been
													// respawned
										}
										Chunk c = loc.getChunk();
										if (c.isLoaded()) {
											c.load(true);
										}
										r.reloadingItemBoxes.remove(signLoc);
										main.listener.spawnItemPickupBox(loc,
												true);
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
		return getRandomPowerup(); //No longer support uCars items
	}

	/*
	public ItemStack getRandomPowerup() {
		PowerupType[] pows = PowerupType.values();
		int min = 0;
		int max = pows.length;
		int randomNumber = plugin.random.nextInt(max - min) + min;
		PowerupType pow = pows[randomNumber];
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
	*/
	public ItemStack getRandomPowerup() {
		List<Class<? extends Powerup>> pows = new ArrayList<Class<? extends Powerup>>();
		pows.add(RedShellPowerup.class);
		pows.add(BlueShellPowerup.class);
		pows.add(GreenShellPowerup.class);
		pows.add(BananaPowerup.class);
		Class<? extends Powerup> rand = pows.get(main.plugin.random.nextInt(pows.size()));
		
		Powerup power = null;
		try {
			power = rand.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return new ItemStack(Material.STONE);
		}
		
		ItemStack i = power.getNewItem();
		
		return i;
	}
	
	
	public Boolean isPlayerImmune(Player player){
		return player.hasMetadata("kart.immune");
	}
	
	public Boolean isCarImmune(Entity carBase){
		return carBase.hasMetadata("kart.immune");
	}

}
