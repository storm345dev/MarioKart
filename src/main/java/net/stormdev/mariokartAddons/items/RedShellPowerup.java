package net.stormdev.mariokartAddons.items;

import net.stormdev.mario.mariokart.User;
import net.stormdev.mario.mariokart.main;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.useful.ucars.ItemStackFromId;

public class RedShellPowerup extends ShellPowerup {
	
	public RedShellPowerup(){
		String id = main.config.getString("mariokart.redShell");
		ItemStack i = ItemStackFromId.get(id);
		//TODO Add lore, etc...
		super.setItemStack(i);
	}

	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc) {
		// TODO
		
	}

	@Override
	public PowerupType getType() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
