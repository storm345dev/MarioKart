package net.stormdev.mario.server;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;

public class VaultEco implements EconProvider {
	private Economy econ;
	
	public VaultEco(Economy econ){
		this.econ = econ;
	}

	@Override
	public double getBalance(Player player) {
		return econ.getBalance(player);
	}

	@Override
	public void spend(Player player, double amount) {
		econ.withdrawPlayer(player, amount);
	}

	@Override
	public void give(Player player, double amount) {
		econ.depositPlayer(player, amount);
	}

}
