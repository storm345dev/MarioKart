package net.stormdev.mariokartAddons.items;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.mariokart.main;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PowerupItem extends ItemStack {
	public PowerupItem(PowerupData powerupRaw) {
		super(powerupRaw.raw.getType());
		this.setDurability(powerupRaw.raw.getDurability());
		this.setAmount(powerupRaw.raw.getAmount());
		PowerupType powerup = powerupRaw.powerup;
		String pow = powerup.toString().toLowerCase();
		if (pow.length() > 1) {
			String body = pow.substring(1);
			String start = pow.substring(0, 1);
			pow = start.toUpperCase() + body;
		}
		ItemMeta meta = this.getItemMeta();
		meta.setDisplayName(main.colors.getInfo() + pow);
		// Set lore based on Item
		List<String> lore = new ArrayList<String>();
		if (powerup == PowerupType.BANANA) {
			lore.add("+Slows players down");
			lore.add("*Right click to deploy");
		} else if (powerup == PowerupType.BLUE_SHELL) {
			lore.add("+Targets and slows the leader");
			lore.add("*Right click to deploy");
		} else if (powerup == PowerupType.BOMB) {
			lore.add("+Throws an ignited bomb");
			lore.add("*Right click to deploy");
		} else if (powerup == PowerupType.BOO) {
			lore.add("+Invisible for 6s");
			lore.add("+Apply nausea to racer ahead");
			lore.add("*Right click to deploy");
		} else if (powerup == PowerupType.GREEN_SHELL) {
			lore.add("+Slows down the victim");
			lore.add("*Left click to throw forwards");
			lore.add("*Right click to throw backwards");
		} else if (powerup == PowerupType.LIGHTNING) {
			lore.add("+Strikes all lightning on enemies");
			lore.add("*Right click to deploy");
		} else if (powerup == PowerupType.MUSHROOM) {
			lore.add("+Applies a short speed boost");
			lore.add("*Right click to use");
		} else if (powerup == PowerupType.POW) {
			lore.add("+Freezes other players");
			lore.add("*Right click to deploy");
		} else if (powerup == PowerupType.RED_SHELL) {
			lore.add("+Slows down the victim");
			lore.add("*Right click to deploy");
		} else if (powerup == PowerupType.STAR) {
			lore.add("+Applies a large speed boost");
			lore.add("+Immunity to other powerups");
			lore.add("*Right click to use");
		} else if (powerup == PowerupType.RANDOM) {
			lore.add("+Gives a random powerup");
			lore.add("*Right click to use");
		}
		meta.setLore(lore);
		this.setItemMeta(meta);
	}
}
