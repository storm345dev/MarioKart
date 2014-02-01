package net.stormdev.mariokartAddons.items;

import org.bukkit.inventory.ItemStack;

import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.utils.ItemStackFromId;

public class PowerupMaker {
	public static PowerupData getPowerupRaw(PowerupType powerup, int amount) {
		String power = "40";
		if (powerup == PowerupType.BANANA) {
			power = main.config.getString("mariokart.banana");
		} else if (powerup == PowerupType.BLUE_SHELL) {
			power = main.config.getString("mariokart.blueShell");
		} else if (powerup == PowerupType.BOMB) {
			power = main.config.getString("mariokart.bomb");
		} else if (powerup == PowerupType.GREEN_SHELL) {
			power = main.config.getString("mariokart.greenShell");
		} else if (powerup == PowerupType.LIGHTNING) {
			power = main.config.getString("mariokart.lightning");
		} else if (powerup == PowerupType.MUSHROOM) {
			power = main.config.getString("mariokart.mushroom");
		} else if (powerup == PowerupType.POW) {
			power = main.config.getString("mariokart.pow");
		} else if (powerup == PowerupType.RED_SHELL) {
			power = main.config.getString("mariokart.redShell");
		} else if (powerup == PowerupType.STAR) {
			power = main.config.getString("mariokart.star");
		} else if (powerup == PowerupType.RANDOM) {
			power = main.config.getString("mariokart.random");
		} else if (powerup == PowerupType.BOO) {
			power = main.config.getString("mariokart.boo");
		}
		PowerupData toReturn = new PowerupData(powerup,
				ItemStackFromId.get(power));
		toReturn.raw.setAmount(amount);
		return toReturn;
	}

	public static ItemStack getPowerup(PowerupType powerup, int amount) {
		PowerupData data = PowerupMaker.getPowerupRaw(powerup, amount);
		PowerupItem item = new PowerupItem(data);
		return item;
	}

}
