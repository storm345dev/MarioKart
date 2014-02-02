package net.stormdev.mario.sound;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.PlayerQuitException;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MusicManager {
	private MarioKart plugin;
	private boolean musicEnabled;
	
	public MusicManager(MarioKart plugin){
		this.plugin = plugin;
		this.musicEnabled = MarioKart.config.getBoolean("general.race.music.enable");
	}
	
	public void playMusic(final Race race){
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new BukkitRunnable(){

			@Override
			public void run() {
				if(!musicEnabled){
					return;
				}
				MarioKartSound song = getBestSong(race);
				for(User u:race.getUsersIn()){
					Player p;
					try {
						p = u.getPlayer();
					} catch (PlayerQuitException e) {
						continue;
					}
					if(p != null && p.isOnline()){
						playCustomSound(p, song);
					}
				}
				return;
			}}, 60l);
	}
	
	public MarioKartSound getBestSong(Race race){
		long raceTime = race.getTimeLimitS() - 75;
		List<MarioKartSound> songs = new ArrayList<MarioKartSound>(MarioKartSound.getMusic());
		for(MarioKartSound song:songs){
			if(song.getLength() > raceTime){
				songs.remove(song);
			}
		}
		if(songs.size() < 1){
			//No songs match, so play the shortest
			return MarioKartSound.MUSIC_MARIOCIRCUIT;
		}
		int pos =  MarioKart.plugin.random.nextInt(songs.size());
		return songs.get(pos);
	}
	
	@SuppressWarnings("deprecation")
	public Boolean playCustomSound(final Player recipient, final Location location, 
			final String soundPath, final float volume, final float pitch){
		MarioKart.plugin.getServer().getScheduler().runTaskAsynchronously(MarioKart.plugin, new BukkitRunnable(){

			@Override
			public void run() {
				//Running async keeps TPS higher
				recipient.playSound(location, soundPath, volume, pitch); //Deprecated but still best way
			}});
		return true;
		/* Not needed
		if(main.prototcolManager == null){
			//No protocolLib
			return false;
		}
		getServer().getScheduler().runTaskAsynchronously(this, new BukkitRunnable(){
			@Override
			public void run() {
				//Play the sound
				try {
					if(pitch > 255){
						pitch = 255;
					}
					PacketContainer customSound = main.prototcolManager.createPacket(PacketType.Play.Server.NAMED_SOUND_EFFECT);
					customSound.getSpecificModifier(String.class).
					    write(0, soundPath);
					customSound.getSpecificModifier(int.class).
					    write(0, location.getBlockX()).
					    write(1, location.getBlockY()).
					    write(2, location.getBlockZ());
					    write(3, (int) pitch);
					customSound.getSpecificModifier(float.class).
					    write(0, volume);
					main.prototcolManager.sendServerPacket(recipient, customSound);
				} catch (Exception e) {
					main.logger.info(main.colors.getError()+"Error playing custom sound: "+soundPath+"!");
					e.printStackTrace();
					return;
				}
				return;
			}});
		return true;
		*/
	}
	
	public Boolean playCustomSound(Player recipient, Location location,
			MarioKartSound sound, float volume, float pitch){
		return playCustomSound(recipient, location, sound.getPath(), volume, pitch);
	}
	
	public Boolean playCustomSound(Player recipient, MarioKartSound sound){
		return playCustomSound(recipient, recipient.getLocation(),
				sound, Float.MAX_VALUE, 1f);
	}
	
}
