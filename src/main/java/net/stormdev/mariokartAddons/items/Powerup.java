package net.stormdev.mariokartAddons.items;

import net.stormdev.mario.mariokart.User;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Powerup {
	public void setItemStack(ItemStack stack);
	public void doRightClickAction(User user, Player player, Minecart car, Location carLoc);
	public PowerupType getType();
	public ItemStack getNewItem();
	public boolean isEqual(ItemStack used);
}
