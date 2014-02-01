package net.stormdev.mariokartAddons.items;


import org.bukkit.util.Vector;

public interface TrackingShell extends Shell {
	public void setTarget(String player);
	public String getTarget();
	public Vector calculateVelocity();
}
