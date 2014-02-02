package net.stormdev.mario.hotbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.shop.Unlockable;
import net.stormdev.mario.shop.Upgrade;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class HotBarManager {

	Map<String, MarioHotBar> hotBars = new HashMap<String, MarioHotBar>();
	private boolean upgrades = true;

	public HotBarManager(boolean upgradesEnabled) {
		this.upgrades = upgradesEnabled;
	}

	public void setHotBar(String player, MarioHotBar hotBar) {
		this.hotBars.put(player, hotBar);
	}

	public MarioHotBar createHotBar(String player) {
		MarioHotBar hotBar = new MarioHotBar(player,
				calculateHotbarContents(player));
		this.hotBars.put(player, hotBar);
		return hotBar;
	}

	public MarioHotBar getHotBar(String player) {
		if (!hotBars.containsKey(player)) {
			return createHotBar(player);
		}
		return hotBars.get(player);
	}

	protected void removeHotBar(String player) {
		hotBars.remove(player);
		return;
	}

	public void clearHotBar(String player) {
		if (hotBars.containsKey(player)) {
			hotBars.get(player).clear();
		}
		return;
	}

	public Map<HotBarSlot, List<HotBarItem>> calculateHotbarContents(
			String player) {
		Map<HotBarSlot, List<HotBarItem>> contents = new HashMap<HotBarSlot, List<HotBarItem>>();
		ArrayList<HotBarItem> defaultItems = new ArrayList<HotBarItem>();
		ArrayList<HotBarItem> unlockedItems = new ArrayList<HotBarItem>();
		HotBarItem exit_door = new HotBarItem(
				new ItemStack(Material.WOOD_DOOR), ChatColor.GREEN
						+ "Leave Race", 1, HotBarUpgrade.LEAVE,
				new HashMap<String, Object>(), "null");
		defaultItems.add(exit_door);
		contents.put(HotBarSlot.UTIL, defaultItems);
		if(upgrades){
		// Look-up purchased upgrades in a menu and add them too
		List<Upgrade> unlocks = MarioKart.plugin.upgradeManager.getUpgrades(player);
		for (Upgrade upgrade : unlocks) {
			Unlockable u = upgrade.getUnlockedAble();
			HotBarItem item = new HotBarItem(new ItemStack(u.displayItem),
					ChatColor.GREEN + u.upgradeName, upgrade.getQuantity(),
					u.type, u.data, u.shortId);
			unlockedItems.add(item);
		}
		}
		contents.put(HotBarSlot.SCROLLER, unlockedItems);
		return contents;
	}
	
	@SuppressWarnings("deprecation")
	public void updateHotBar(Player player) {
		MarioHotBar hotBar = MarioKart.plugin.hotBarManager.getHotBar(player
				.getName());
		HotBarItem util = hotBar.getDisplayedItem(HotBarSlot.UTIL);
		HotBarItem scroller = hotBar.getDisplayedItem(HotBarSlot.SCROLLER);
		if (util != null) {
			player.getInventory().setItem(7, util.getDisplayItem());
		} else {
			player.getInventory().setItem(7, new ItemStack(Material.AIR));
		}
		if (scroller != null) {
			player.getInventory().setItem(6, scroller.getDisplayItem());
		} else {
			player.getInventory().setItem(6, new ItemStack(Material.AIR));
		}
		player.getInventory().setItem(8, MarioKart.powerupManager.respawn);
		player.updateInventory();
		return;
	}
	
	public void executeClick(final Player player, MarioHotBar hotBar, HotBarSlot slot){
		HotBarItem hotBarItem = hotBar.getDisplayedItem(slot);
		Map<String, Object> data = hotBarItem.getData();
		HotBarUpgrade type = hotBarItem.getType();
		@SuppressWarnings("unused")
		String upgradeName = "Unknown";
		Boolean useUpgrade = true;
		Boolean execute = true;
		if (data.containsKey("upgrade.name")) {
			upgradeName = data.get("upgrade.name").toString();
		}
		if (type == HotBarUpgrade.LEAVE) {
			// Make the player leave the race
			MarioKart.plugin.raceCommandExecutor.urace(player, new String[] { "leave" }, player);
			return;
		} else if (type == HotBarUpgrade.SPEED_BOOST) {
			long lengthMS = 5000;
			double power = 5;
			Boolean useItem = true;
			if (data.containsKey("upgrade.length")) {
				lengthMS = (Long) data.get("upgrade.length");
			}
			if (data.containsKey("upgrade.power")) {
				power = (Double) data.get("upgrade.power");
			}
			if (data.containsKey("upgrade.useItem")) {
				useItem = (Boolean) data.get("upgrade.useItem");
			}
			if (data.containsKey("upgrade.useUpgrade")) {
				useUpgrade = (Boolean) data.get("upgrade.useUpgrade");
			}
			if (useItem) {
				if (!hotBar.useItem(slot)) {
					execute = false;
				}
			}
			if (execute) {
				ucars.listener.carBoost(player.getName(), power, lengthMS,
						ucars.config.getDouble("general.cars.defSpeed"));
			}
		} else if (type == HotBarUpgrade.IMMUNITY) {
			long lengthMS = 5000;
			Boolean useItem = true;
			if (data.containsKey("upgrade.length")) {
				lengthMS = (Long) data.get("upgrade.length");
			}
			if (data.containsKey("upgrade.useItem")) {
				useItem = (Boolean) data.get("upgrade.useItem");
			}
			if (data.containsKey("upgrade.useUpgrade")) {
				useUpgrade = (Boolean) data.get("upgrade.useUpgrade");
			}
			if (useItem) {
				if (!hotBar.useItem(slot)) {
					execute = false;
				}
			}
			if (execute) {
				if (player.getVehicle() == null) {
					return;
				}
				final Entity veh = player.getVehicle();
				veh.setMetadata("kart.immune", new StatValue(lengthMS, MarioKart.plugin));
				player.setMetadata("kart.immune", new StatValue(lengthMS, MarioKart.plugin));
				MarioKart.plugin.getServer().getScheduler()
						.runTaskLater(MarioKart.plugin, new Runnable() {

							@Override
							public void run() {
								try {
									player.removeMetadata("kart.immune", MarioKart.plugin);
									veh.removeMetadata("kart.immune",
											MarioKart.plugin);
									player.getWorld().playSound(
											player.getLocation(), Sound.CLICK,
											0.5f, 3f);
								} catch (Exception e) {
									// Player or vehicle are gone
								}
								return;
							}
						}, (long) (lengthMS * 0.020));
				player.getWorld().playSound(player.getLocation(), Sound.DRINK,
						0.5f, 3f);
			}
		}
		if (useUpgrade && execute) {
			if (MarioKart.plugin.upgradeManager.useUpgrade(
					player.getName(),
					new Upgrade(MarioKart.plugin.upgradeManager
							.getUnlockable(hotBarItem.shortId), 1))) {
				player.sendMessage(MarioKart.msgs.get("race.upgrades.use"));
				updateHotBar(player);
				return;
			}
		}
		updateHotBar(player);
		return;
	}

}
