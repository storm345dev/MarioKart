package net.stormdev.mariokartAddons.items;

import org.bukkit.inventory.ItemStack;

public abstract class PowerupBase implements Powerup {
	protected ItemStack stack = null;
	protected String owner = null;

	@Override
	public ItemStack getNewItem() {
		//Shells can be between 1 and 3 in quantity
		ItemStack s = stack.clone();
		
		return s;
	}
	
	@Override
	public void setItemStack(ItemStack item){
		stack = item;
	}

	@Override
	public boolean isEqual(ItemStack used) {
		return used.isSimilar(stack);
	}
	
	@Override
	public String getOwner(){
		return owner;
	}
	
	@Override
	public boolean isOwner(String player){
		return getOwner().equals(player);
	}
	
	@Override
	public void setOwner(String player){
		this.owner = player;
	}
}
