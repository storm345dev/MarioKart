package net.stormdev.mario.utils;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.User;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RaceFinishEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Race race = null;
	private User user;

	public RaceFinishEvent(Race race, User user) {
		this.race = race;
		this.user = user;
	}

	public Race getRace() {
		return this.race;
	}

	public User getUser() {
		return user;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
