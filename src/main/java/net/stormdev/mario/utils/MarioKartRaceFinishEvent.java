package net.stormdev.mario.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event to be used to dish out rewards, etc... at the end of a race
 */
public class MarioKartRaceFinishEvent extends Event implements Cancellable {
	public Boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();
	Player player = null;
	int position = 1;
	String pos = "";

	public MarioKartRaceFinishEvent(Player player, int position,
			String positionFriendly) {
		this.player = player;
		this.position = position;
		this.pos = positionFriendly;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

	public Integer getFinishPosition() {
		return position;
	}

	public String getPlayerFriendlyPosition() {
		return pos;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
