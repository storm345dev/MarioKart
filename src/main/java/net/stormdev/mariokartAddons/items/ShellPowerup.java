package net.stormdev.mariokartAddons.items;

import net.stormdev.ucars.trade.main;

import org.bukkit.inventory.ItemStack;

public abstract class ShellPowerup implements Powerup {
	private ItemStack stack = null;

	public void setItemStack(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public ItemStack getNewItem() {
		//Shells can be between 1 and 3 in quantity
		ItemStack s = stack.clone();
		
		int rand = main.random.nextInt(6); //Between 0 and 5
		rand -= 2; //Between -2 and 3
		if(rand < 1)
			rand = 1;
		
		s.setAmount(rand);
		
		return s;
	}

	@Override
	public boolean isEqual(ItemStack used) {
		return used.isSimilar(stack);
	}

}
