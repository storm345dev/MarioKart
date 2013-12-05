package net.stormdev.mario.utils;

import org.bukkit.inventory.ItemStack;

public class HotBarItem {
	private ItemStack displayItem = null;
	private String displayName = "";
	private int quantity = 1;
	private HotBarUpgrade type = HotBarUpgrade.SPEED_BOOST;
	public HotBarItem(ItemStack displayItem, String displayName, int quantity, HotBarUpgrade type){
		this.displayItem = displayItem;
		this.displayName = displayName;
		this.quantity = quantity;
		this.type = type;
	}
	public ItemStack getDisplayItem(){
		return this.displayItem;
	}
	public void setDisplayItem(ItemStack item){
		this.displayItem = item;
	}
	public String getDisplayName(){
		return this.displayName;
	}
	public void setDisplayName(String name){
	    this.displayName = name;
	}
	public int getQuantity(){
		return this.quantity;
	}
	public void setQuantity(int amount){
		this.quantity = amount;
	}
	public HotBarUpgrade getType(){
		return this.type;
	}
	public void setType(HotBarUpgrade type){
		this.type = type;
	}

}
