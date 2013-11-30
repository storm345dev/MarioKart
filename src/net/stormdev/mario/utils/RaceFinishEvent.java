package net.stormdev.mario.utils;

import net.stormdev.mario.mariokart.Race;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RaceFinishEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Race race = null;
	public String playername = "";

	public RaceFinishEvent(Race race, String playername) {
		this.race = race;
		this.playername = playername;
	}

	public Race getRace() {
		return this.race;
	}

	public String getPlayername() {
		return this.playername;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
