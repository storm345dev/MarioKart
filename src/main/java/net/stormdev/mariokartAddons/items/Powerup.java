package net.stormdev.mariokartAddons.items;

import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Powerup {
	public void setItemStack(ItemStack stack);
	public void doRightClickAction(User user, Player player, Minecart car, 
			Location carLoc, Race race, ItemStack inHand);
	public void doLeftClickAction(User user, Player player, Minecart car, 
			Location carLoc, Race race, ItemStack inHand);
	public PowerupType getType();
	public ItemStack getNewItem();
	public boolean isEqual(ItemStack used);
	public String getOwner();
	public void setOwner(String player);
	public boolean isOwner(String player);
}
