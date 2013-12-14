package net.stormdev.mario.utils;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SelectMenuClickEvent extends Event implements Cancellable {
	public Boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();
	IconMenu.OptionClickEvent clickEvent = null;
	int page = 1;
	Object[] args = new Object[] {};
	SelectMenuType type = SelectMenuType.MENU;

	public SelectMenuClickEvent(IconMenu.OptionClickEvent clickEvent,
			SelectMenuType type, int absolutePageNumber) {
		this.clickEvent = clickEvent;
		this.page = absolutePageNumber;
		this.type = type;
	}

	public SelectMenuClickEvent(IconMenu.OptionClickEvent clickEvent,
			SelectMenuType type, int absolutePageNumber, Object[] args) {
		this.clickEvent = clickEvent;
		this.page = absolutePageNumber;
		this.type = type;
		this.args = args;
	}

	public Object[] getArgs() {
		return args;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

	public SelectMenuType getMenuType() {
		return type;
	}

	public int getPage() {
		return page;
	}

	public IconMenu.OptionClickEvent getClickEvent() {
		return clickEvent;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
