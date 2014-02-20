package net.stormdev.mario.sound;

import java.util.ArrayList;
import java.util.List;

public class MarioKartSound {
	public static final MarioKartSound BANANA_HIT = new MarioKartSound("mariokart.sfx.banana");
	public static final MarioKartSound COUNTDOWN_PLING = new MarioKartSound("mariokart.sfx.startCountdown");
	public static final MarioKartSound ONE_UP = new MarioKartSound("mariokart.sfx.1up");
	public static final MarioKartSound ITEM_SELECT_BEEP = new MarioKartSound("mariokart.sfx.itemSelectBeep");
	public static final MarioKartSound LAST_LAP = new MarioKartSound("mariokart.sfx.lastLap");
	public static final MarioKartSound PENALTY_END = new MarioKartSound("mariokart.sfx.penaltyEnd");
	public static final MarioKartSound RACE_LOSE = new MarioKartSound("mariokart.sfx.lose");
	public static final MarioKartSound RACE_START_COUNTDOWN = new MarioKartSound("mariokart.sfx.raceStartup");
	public static final MarioKartSound RACE_WIN = new MarioKartSound("mariokart.sfx.win");
	public static final MarioKartSound SHELL_HIT = new MarioKartSound("mariokart.sfx.shellHit");
	public static final MarioKartSound STAR_RIFF = new MarioKartSound("mariokart.sfx.star");
	public static final MarioKartSound TRACKING_BLEEP = new MarioKartSound("mariokart.sfx.trackingBleep");
	//public static final MarioKartSound MUSIC_MARIOCIRCUIT = new MarioKartSound("mariokart.music.marioCircuit", 65.4);
	//public static final MarioKartSound MUSIC_MARIOCIRCUITORCHESTRA = new MarioKartSound("mariokart.music.marioCircuitOrchestra", 210);
	//public static final MarioKartSound MUSIC_RACEWAY = new MarioKartSound("mariokart.music.raceWay", 145.8);
	
	private String path = "";
	private double length = 0; //In s
	private MarioKartSound(String path){
		this.path = path;
	}
	protected MarioKartSound(String path, double length){
		this.path = path;
		this.length = length;
	}
	public String getPath(){
		return path;
	}
	public double getLength(){ //In s
		return length;
	}
	@Override
	public String toString(){
		return path;
	}
	
	/*
	public static final List<MarioKartSound> getMusic(){
		List<MarioKartSound> list = new ArrayList<MarioKartSound>();
		list.add(MUSIC_MARIOCIRCUIT);
		list.add(MUSIC_MARIOCIRCUITORCHESTRA);
		list.add(MUSIC_RACEWAY);
		return list;
	}
	*/
}
