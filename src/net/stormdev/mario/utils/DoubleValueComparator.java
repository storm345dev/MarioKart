package net.stormdev.mario.utils;

import java.util.Comparator;
import java.util.Map;

import org.bukkit.entity.Player;

public class DoubleValueComparator implements Comparator<Player> {

	Map<Player, Double> base;

	public DoubleValueComparator(Map<Player, Double> scores) {
		this.base = scores;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(Player a, Player b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}