package net.stormdev.mario.utils;

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
	public static final MarioKartSound MUSIC_MARIOCIRCUIT = new MarioKartSound("mariokart.music.marioCircuit");
	public static final MarioKartSound MUSIC_MARIOCIRCUITORCHESTRA = new MarioKartSound("mariokart.music.marioCircuitOrchestra");
	public static final MarioKartSound MUSIC_RACEWAY = new MarioKartSound("mariokart.music.raceWay");
	
	private String path = "";
	private MarioKartSound(String path){
		this.path = path;
	}
	public String getPath(){
		return path;
	}
	@Override
	public String toString(){
		return path;
	}
}
