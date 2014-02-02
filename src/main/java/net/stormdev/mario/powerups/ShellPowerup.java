package net.stormdev.mario.powerups;

import net.stormdev.mario.mariokart.main;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ShellPowerup implements Powerup, Shell {
	private ItemStack stack = null;
	protected Item item = null;
	protected String owner = null;
	private int expiry = 33;
	private int cooldown = 0;

	public void setItemStack(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public ItemStack getNewItem() {
		//Shells can be between 1 and 3 in quantity
		ItemStack s = stack.clone();
		
		int rand = main.plugin.random.nextInt(6); //Between 0 and 5
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
	
	@Override
	public void spawn(Location loc, Player owner){
		if(isFired())
			return;
		
		this.owner = owner.getName(); //Set owner
		
		//Spawn in a shell
		item = loc.getWorld().dropItem(loc, stack);
	}
	
	@Override
	public boolean isFired(){
		return item != null;
	}
	
	@Override
	public Item getFiredItem(){
		return item;
	}
	
	@Override
	public int getRemainingCooldown(){
		return this.cooldown;
	}
	
	@Override
	public void setCooldown(int cooldown){
		this.cooldown = cooldown;
	}
	
	@Override
	public boolean isCooldown(){
		return this.cooldown > 0;
	}
	
	@Override
	public int getRemainingExpiry(){
		return this.expiry;
	}
	
	@Override
	public void setExpiry(int expiry){
		this.expiry = expiry;
	}
	
	@Override
	public boolean isExpired(){
		return !(this.expiry > 0);
	}
	
	@Override
	public boolean remove(){
		if(!isExpired()){
			return false;
		}
		item.remove();
		item = null;
		owner = null;
		return true;
	}
	
	@Override
	public void decrementCooldown(){
		cooldown--;
		if(cooldown < 0)
			cooldown = 0;
	}
	
	@Override
	public void decrementExpiry(){
		expiry--;
		if(expiry < 0){
			expiry = 0;
			remove();
		}
	}

}
