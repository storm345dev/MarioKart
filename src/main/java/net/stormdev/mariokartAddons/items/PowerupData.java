package net.stormdev.mariokartAddons.items;

import org.bukkit.inventory.ItemStack;

public class PowerupData {
	public PowerupType powerup = null;
	public ItemStack raw = null;

	public PowerupData(PowerupType powerup, ItemStack raw) {
		this.powerup = powerup;
		this.raw = raw;
	}

}
