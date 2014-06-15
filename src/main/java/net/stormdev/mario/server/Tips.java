package net.stormdev.mario.server;

import java.util.Random;

public class Tips {
	private static final String[] tips = new String[]{
		"Use brake (d) to get around sharp corners!",
		"Use brake (d) to block against POW blocks!",
		"Hold forwards 3s before the start for a boost!",
		"Right click to use a powerup!",
		"Drive over a box to get a powerup!",
		"Use the door to quit back to the lobby!",
		"Use the egg to respawn if you get stuck or lost!",
		"Buy upgrades in the race shop with tokens!",
		"Win the race to get tokens!",
		"Drive over gold and diamond blocks to boost!",
		"Use left click to fire green shells forwards!",
		"Use right click to fire green shells backwards!",
		"Make sure sound is on when you play!",
		"Make sure particles are on when you play!"
	};
	
	private static String tip = random();
	private static Random rand = new Random();
	
	public static String random(){
		tip = tips[rand.nextInt(tips.length)];
		return tip;
	}
	
	public static String getCurrentTip(){
		return tip;
	}
	
}
