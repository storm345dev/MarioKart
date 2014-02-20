package net.stormdev.mario.sound;

public class MarioKartSong {
	private MarioKartSound sound;
	private String name;
	public MarioKartSong(String name, String path, double length){
		this.sound = new MarioKartSound(path, length);
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public MarioKartSound asMkSound(){
		return sound;
	}
	
	public double getLength(){
		return sound.getLength();
	}
	
	public String getSoundPath(){
		return sound.getPath();
	}
}
