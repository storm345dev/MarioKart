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
	public void setHotBar(Map<HotBarSlot, List<HotBarItem>> hotBar){
		this.hotBar = hotBar;
		return;
	}
	public void setHotBarItems(HotBarSlot slot, List<HotBarItem> items){
		hotBar.put(slot, items);
	}
	public void addHotBarItem(HotBarSlot slot, HotBarItem item){
		List<HotBarItem> items = new ArrayList<HotBarItem>();
		if(hotBar.containsKey(slot)){
			items.addAll(hotBar.get(slot));
		}
		items.add(item);
		hotBar.put(slot, items);
		return;
	}
	public void clearSlot(HotBarSlot slot){
		this.hotBar.remove(slot);
		return;
	}
	public Boolean useItem(HotBarSlot slot){
		HotBarItem item = getDisplayedItem(slot);
		if(item == null){
			return false;
		}
		int q = item.getQuantity();
		if(q-1<0){
			return false;
		}
		item.setQuantity(q-1);
		if(!hotBar.containsKey(slot)){
			return false;
		}
		List<HotBarItem> items = hotBar.get(slot);
		if(items.size() < 1){
			return false;
		}
		items.set(0, item);
		hotBar.put(slot, items);
		return true;
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
