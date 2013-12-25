package net.stormdev.mario.utils;

public class MarioKartSound {
	public static final MarioKartSound ONE_UP = new MarioKartSound("mariokart.sfx.1up");
	public static final MarioKartSound ITEM_SELECT_BEEP = new MarioKartSound("mariokart.sfx.itemSelectBeep");
	
	
	private String path = "";
	private MarioKartSound(String path){
		this.path = path;
	}
	public String getPath(){
		return path;
	}
	public String toString(){
		return path;
	}
}
