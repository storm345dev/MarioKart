package net.stormdev.mario.utils;

import net.stormdev.mario.mariokart.Race;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RaceEndEvent extends Event { // Called when a race is ended
	private static final HandlerList handlers = new HandlerList();
	private Race race = null;
	public RaceEndEvent(Race race) {
		this.race = race;
	}

	public Race getRace() {
		return this.race;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
