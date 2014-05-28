package net.stormdev.mario.races;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event to be used to dish out rewards, etc... at the end of a race
 */
public class MarioKartRaceEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	public Race race;

	public MarioKartRaceEndEvent(Race race) {
		this.race = race;
	}

	public Race getRace(){
		return race;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
