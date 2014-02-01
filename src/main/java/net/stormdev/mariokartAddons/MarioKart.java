package net.stormdev.mariokartAddons;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.hotbar.HotBarSlot;
import net.stormdev.mario.hotbar.MarioHotBar;
import net.stormdev.mario.items.ItemStacks;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.sound.MarioKartSound;
import net.stormdev.mariokartAddons.items.BananaPowerup;
import net.stormdev.mariokartAddons.items.BlueShellPowerup;
import net.stormdev.mariokartAddons.items.BombPowerup;
import net.stormdev.mariokartAddons.items.BooPowerup;
import net.stormdev.mariokartAddons.items.BoxPowerup;
import net.stormdev.mariokartAddons.items.GreenShellPowerup;
import net.stormdev.mariokartAddons.items.LightningPowerup;
import net.stormdev.mariokartAddons.items.MushroomPowerup;
import net.stormdev.mariokartAddons.items.PowPowerup;
import net.stormdev.mariokartAddons.items.Powerup;
import net.stormdev.mariokartAddons.items.RedShellPowerup;
import net.stormdev.mariokartAddons.items.StarPowerup;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class MarioKart {
	main plugin = null;
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
			Powerup powerup = null;
			
			if(BananaPowerup.isItemSimilar(inHand)){
				powerup = new BananaPowerup();
			}
			else if(BlueShellPowerup.isItemSimilar(inHand)){
				powerup = new BlueShellPowerup();
			}
			else if(BombPowerup.isItemSimilar(inHand)){
				powerup = new BombPowerup();
			}
			else if(BooPowerup.isItemSimilar(inHand)){
				powerup = new BooPowerup();
			}
			else if(BoxPowerup.isItemSimilar(inHand)){
				powerup = new BoxPowerup();
			}
			else if(GreenShellPowerup.isItemSimilar(inHand)){
				powerup = new GreenShellPowerup();
			}
			else if(LightningPowerup.isItemSimilar(inHand)){
				powerup = new LightningPowerup();
			}
			else if(MushroomPowerup.isItemSimilar(inHand)){
				powerup = new MushroomPowerup();
			}
			else if(PowPowerup.isItemSimilar(inHand)){
				powerup = new PowPowerup();
			}
			else if(RedShellPowerup.isItemSimilar(inHand)){
				powerup = new RedShellPowerup();
			}
			else if(StarPowerup.isItemSimilar(inHand)){
				powerup = new StarPowerup();
			}
			
			if(powerup != null){
				powerup.setOwner(player.getName());
				powerup.doRightClickAction(race.getUser(player), player, car, car.getLocation(), race, inHand);
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
									while (ItemStacks.equals(main.config
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
									while (ItemStacks.equals(main.config
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
		pows.add(BombPowerup.class);
		pows.add(BooPowerup.class);
		pows.add(BoxPowerup.class);
		pows.add(LightningPowerup.class);
		pows.add(MushroomPowerup.class);
		pows.add(PowPowerup.class);
		pows.add(StarPowerup.class);
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
