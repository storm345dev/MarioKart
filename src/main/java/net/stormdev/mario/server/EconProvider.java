package net.stormdev.mario.server;

import org.bukkit.entity.Player;

public interface EconProvider {
	public double getBalance(Player player);
	public void spend(Player player, double amount);
	public void give(Player player, double amount);
}
