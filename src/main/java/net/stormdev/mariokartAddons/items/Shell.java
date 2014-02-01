package net.stormdev.mariokartAddons.items;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public interface Shell {
	public void move();
	public void start();
	public void spawn(Location loc, Player owner);
	public void collide(Player player);
	public boolean isFired();
	public Item getFiredItem();
	public int getRemainingCooldown();
	public void setCooldown(int cooldown);
	public boolean isCooldown();
	public void decrementCooldown();
	public int getRemainingExpiry();
	public void setExpiry(int expiry);
	public boolean isExpired();
	public boolean remove();
	public void decrementExpiry();
}
