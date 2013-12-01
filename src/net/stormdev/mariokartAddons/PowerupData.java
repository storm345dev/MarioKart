package net.stormdev.mariokartAddons;

import org.bukkit.inventory.ItemStack;

public class PowerupData {
	public Powerup powerup = null;
	public ItemStack raw = null;

	public PowerupData(Powerup powerup, ItemStack raw) {
		this.powerup = powerup;
		this.raw = raw;
	}

}
