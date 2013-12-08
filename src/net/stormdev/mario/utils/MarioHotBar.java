package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

//TODO
public class MarioHotBar {
	private Map<HotBarSlot, List<HotBarItem>> hotBar = new HashMap<HotBarSlot, List<HotBarItem>>();
	private Player player = null;
	public MarioHotBar(Player player, Map<HotBarSlot, List<HotBarItem>> items){
    	this.player = player;
    	this.hotBar = items;
    }
	public Player getPlayer(){
		return player;
	}
	public Map<HotBarSlot, List<HotBarItem>> getHotBar(){
		return new HashMap<HotBarSlot, List<HotBarItem>>(hotBar);
	}
	public List<HotBarItem> getHotBarItems(HotBarSlot slot){
		return new ArrayList<HotBarItem>(hotBar.get(slot));
	}
	public void scroll(HotBarSlot slot){
		if(!hotBar.containsKey(slot)){
			return;
		}
		List<HotBarItem> items = hotBar.get(slot);
		if(items.size() < 2){
			return;
		}
		HotBarItem i = items.get(0);
		items.remove(0);
		items.add(i); //Put what had been on display to the back of the item list
		hotBar.put(slot, items);
	}
	public HotBarItem getDisplayedItem(HotBarSlot slot){
		if(!hotBar.containsKey(slot)){
			return null;
		}
		List<HotBarItem> items = hotBar.get(slot);
		if(items.size() < 1){
			return null;
		}
		HotBarItem i = items.get(0);
		String n = i.getDisplayName();
		if(items.size() > 1){
			n = n+ChatColor.RESET+ChatColor.DARK_PURPLE+"(Press 'd' to change)";
		}
		ItemStack item = i.getDisplayItem();
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(n);
		item.setItemMeta(im);
		int q = i.getQuantity();
		if(q>64){
			q = 64;
		}
		item.setAmount(q);
		i.setDisplayItem(item);
		return i; //The front most item
	}
	/**
	 * Mandatory be called when Object's life has ended, to fix memory leaks
	 */
	public void clear(){
		this.player = null; //Plug memory leak
		this.hotBar.clear();
	}
}
