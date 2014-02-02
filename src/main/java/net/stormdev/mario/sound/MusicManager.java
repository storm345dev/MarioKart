package net.stormdev.mario.sound;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.PlayerQuitException;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;

import org.bukkit.Bukkit;
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
						MarioKart.plugin.playCustomSound(p, song);
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
	
}
