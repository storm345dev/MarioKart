package net.stormdev.mario.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

//TODO
public class MarioHotBar {
	private Map<HotBarSlot, List<HotBarItem>> hotBar = new HashMap<HotBarSlot, List<HotBarItem>>();
	private Player player = null;
	public MarioHotBar(Player player, Map<HotBarSlot, List<HotBarItem>> items){
    	this.player = player;
    	this.hotBar = items;
    }
	/**
	 * Mandatory be called when Object's life has ended, to fix memory leaks
	 */
	public void clear(){
		this.player = null; //Fix memory leak
		this.hotBar.clear();
	}
}
