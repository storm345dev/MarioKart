package net.stormdev.mario.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event to be used to dish out rewards, etc... at the end of a race
 */
public class MarioKartHotBarClickEvent extends Event{
    public Boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private MarioHotBar hotBar = null;
    private HotBarSlot clickedSlot = HotBarSlot.UTIL;
	public MarioKartHotBarClickEvent(Player player, MarioHotBar hotBar, HotBarSlot clickedSlot) {
		this.player = player;
		this.hotBar = hotBar;
		this.clickedSlot = clickedSlot;
	}
	public boolean isCancelled() {
		return this.cancelled;
	}
	public MarioHotBar getHotBar(){
		return hotBar;
	}
	public HotBarSlot getHotBarSlot(){
		return clickedSlot;
	}
	public Player getPlayer(){
		return player;
	}
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}
}
