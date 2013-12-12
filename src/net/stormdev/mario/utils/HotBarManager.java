package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.stormdev.mario.mariokart.main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class HotBarManager {
	//TODO
	
	Map<String, MarioHotBar> hotBars = new HashMap<String, MarioHotBar>();
	public HotBarManager(){
		
	}
	public void setHotBar(String player, MarioHotBar hotBar){
		this.hotBars.put(player, hotBar);
	}
	public MarioHotBar createHotBar(String player){
		MarioHotBar hotBar = new MarioHotBar(player, calculateHotbarContents(player));
		this.hotBars.put(player, hotBar);
		return hotBar;
	}
	public MarioHotBar getHotBar(String player){
		if(!hotBars.containsKey(player)){
			return createHotBar(player);
		}
		return hotBars.get(player);
	}
	protected void removeHotBar(String player){
		hotBars.remove(player);
		return;
	}
	public void clearHotBar(String player){
		if(hotBars.containsKey(player)){
			hotBars.get(player).clear();
		}
		return;
	}
	public Map<HotBarSlot, List<HotBarItem>> calculateHotbarContents(String player){
		Map<HotBarSlot, List<HotBarItem>>  contents = new HashMap<HotBarSlot, List<HotBarItem>>();
		ArrayList<HotBarItem> defaultItems = new ArrayList<HotBarItem>();
		ArrayList<HotBarItem> unlockedItems = new ArrayList<HotBarItem>();
		HotBarItem exit_door = new HotBarItem(new ItemStack(Material.WOOD_DOOR), 
				ChatColor.GREEN+"Leave Race", 1, 
				HotBarUpgrade.LEAVE, new HashMap<String, Object>(), "null");
		defaultItems.add(exit_door);
		contents.put(HotBarSlot.UTIL, defaultItems);
		//Look-up purchased upgrades in a menu and add them too
		List<Upgrade> unlocks = main.plugin.upgradeManager.getUpgrades(player);
		for(Upgrade upgrade:unlocks){
			Unlockable u = upgrade.getUnlockedAble();
			HotBarItem item = new HotBarItem(new ItemStack(u.displayItem),
					ChatColor.GREEN+u.upgradeName, upgrade.getQuantity(), 
					u.type, u.data, u.shortId);
			unlockedItems.add(item);
		}
		contents.put(HotBarSlot.SCROLLER, unlockedItems);
		return contents;
	}

}
