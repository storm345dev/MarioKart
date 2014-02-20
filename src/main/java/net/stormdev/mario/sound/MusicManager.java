package net.stormdev.mario.sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.PlayerQuitException;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MusicManager {
	private MarioKart plugin;
	private boolean musicEnabled;
	private Map<String, MarioKartSong> songs = new HashMap<String, MarioKartSong>();
	
	public MusicManager(MarioKart plugin){
		this.plugin = plugin;
		this.musicEnabled = MarioKart.config.getBoolean("general.race.music.enable");
		loadFromCfg(MarioKart.config);
	}
	
	private void loadFromCfg(FileConfiguration config){
		if(!config.contains("music.configVersion")){
			//set defaults...
			
			config.set("music.configVersion", 1.0d);
			
			config.set("music.tracks.defaultSong1.length", 69.0); // 1 min, 9s
			config.set("music.tracks.defaultSong1.path", "mariokart.music.music1");
			
			config.set("music.tracks.defaultSong2.length", 163.0); //2 min, 43s
			config.set("music.tracks.defaultSong2.path", "mariokart.music.music2");
			
			config.set("music.tracks.defaultSong3.length", 230.0); //3 min, 50s
			config.set("music.tracks.defaultSong3.path", "mariokart.music.music3");
			MarioKart.plugin.saveConfig();
		}
		
		ConfigurationSection musics = config.getConfigurationSection("music.tracks");
		Set<String> songs = musics.getKeys(false);
		for(String name:songs){
			ConfigurationSection trackData = musics.getConfigurationSection(name);
			double length = trackData.getDouble("length");
			String path = trackData.getString("path");
			if(length > 0 && path != null){
				MarioKartSong song = new MarioKartSong(name, path, length);
				this.songs.put(name, song);
			}
		}
		
		MarioKart.logger.info("Loaded "+songs.size()+" songs!");
	}
	
	public void playMusic(final Race race){
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new BukkitRunnable(){

			@Override
			public void run() {
				if(!musicEnabled){
					return;
				}
				MarioKartSound song = getBestSong(race);
				if(song == null){
					return;
				}
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
	
	//Song chooser
	
	
	public MarioKartSound getBestSong(Race race){ //TODO
		long raceTime = race.getTimeLimitS() - 75;
		List<MarioKartSong> songs = new ArrayList<MarioKartSong>(this.songs.values());
		for(MarioKartSong song:new ArrayList<MarioKartSong>(songs)){ //Key is also the song length
			if(song.getLength() > raceTime){
				songs.remove(song);
			}
		}
		if(songs.size() < 1){
			//No songs match, so play a random one
			if(this.songs.size() < 1){ //No songs to play
				return null;
			}
			return this.songs.get(MarioKart.plugin.random.nextInt(this.songs.size())).asMkSound();
		}
		int pos =  MarioKart.plugin.random.nextInt(songs.size());
		return songs.get(pos).asMkSound();
	}
	
	//Sound player
	
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
