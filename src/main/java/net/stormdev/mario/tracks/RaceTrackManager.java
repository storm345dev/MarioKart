package net.stormdev.mario.tracks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import net.stormdev.mario.mariokart.main;

public class RaceTrackManager {
	main main = null;
	File saveFile = null;
	ArrayList<RaceTrack> tracks = new ArrayList<RaceTrack>();

	public RaceTrackManager(main main, File saveFile) {
		this.main = main;
		this.saveFile = saveFile;
		if (this.saveFile.exists() == false) {
			try {
				this.saveFile.getParentFile().mkdirs();
				this.saveFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		load();
	}

	public synchronized void save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(this.saveFile));
			oos.writeObject(tracks);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void load() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					this.saveFile));
			Object result = ois.readObject();
			ois.close();
			loadAndConvert(result);
		} catch (Exception e) {
			System.out.println("Attention! No race tracks were loaded! If you created race tracks previously,"
					+ " they are in the old format; please re-create them. Sorry for the inconvenience.");
			try {
				Thread.sleep(1000); //Wait for them to read it
			} catch (InterruptedException e1) {}
			save(); //Save a blank list to override the old file to stop message showing again
			// File just created
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadAndConvert(Object result){
		//Result is ArrayList<RaceTrack>
		System.out.println("Loading tracks...");
		if(!(result instanceof ArrayList)){
			System.out.println("Failed to load Race Tracks, not a list!");
			return;
		}
		ArrayList<?> tr = (ArrayList<?>) result; // '?' as type cannot be guanteed at runtime
		if(tr.size() < 1){
			return;
		}
		Object first = tr.get(0);
		if(first instanceof RaceTrack){
			tracks = (ArrayList<RaceTrack>) result; //Load it!
			return; 
		}
		
		tracks = new ArrayList<RaceTrack>(); //Clear tracks
		//Need to convert it from old format
		System.out.println("Converting tracks...");
		
		for(Object instance:tr){ //'instance' is a track in the old format
			try {
				@SuppressWarnings("rawtypes")
				Class c = instance.getClass();
				String name = "";
				int maxPlayers = 0;
				int minPlayers = 2;
				int laps = 1;
				if(c.getField("trackname") != null){
					Field f = c.getField("trackname");
					f.setAccessible(true);
					name = f.get(instance).toString();
				}
				if(c.getField("maxplayers") != null){
					Field f = c.getField("maxplayers");
					f.setAccessible(true);
					maxPlayers = f.getInt(instance);
				}
				if(c.getField("minplayers") != null){
					Field f = c.getField("minplayers");
					f.setAccessible(true);
					minPlayers = f.getInt(instance);
				}
				if(c.getField("laps") != null){
					Field f = c.getField("laps");
					f.setAccessible(true);
					laps = f.getInt(instance);
				}
				RaceTrack track = new RaceTrack(name, maxPlayers, minPlayers, laps);
				@SuppressWarnings("rawtypes")
				Class latest = track.getClass();
				//Fill track values
				if(c.getField("lobby") != null){
					Field f = c.getField("lobby");
					f.setAccessible(true);
					Field l = latest.getField("lobby");
					l.setAccessible(true);
					l.set(track, f.get(instance)); //Update the value on the new format
				}
				if(c.getField("exit") != null){
					Field f = c.getField("exit");
					f.setAccessible(true);
					Field l = latest.getField("exit");
					l.setAccessible(true);
					l.set(track, f.get(instance)); //Update the value on the new format
				}
				if(c.getField("line1") != null){
					Field f = c.getField("line1");
					f.setAccessible(true);
					Field l = latest.getField("line1");
					l.setAccessible(true);
					l.set(track, f.get(instance)); //Update the value on the new format
				}
				if(c.getField("line2") != null){
					Field f = c.getField("line2");
					f.setAccessible(true);
					Field l = latest.getField("line2");
					l.setAccessible(true);
					l.set(track, f.get(instance)); //Update the value on the new format
				}
				if(c.getField("startGrid") != null){
					Field f = c.getField("startGrid");
					f.setAccessible(true);
					Field l = latest.getField("startGrid");
					l.setAccessible(true);
					l.set(track, f.get(instance)); //Update the value on the new format
				}
				if(c.getField("checkPoints") != null){
					Field f = c.getField("checkPoints");
					f.setAccessible(true);
					Field l = latest.getField("checkPoints");
					l.setAccessible(true);
					l.set(track, f.get(instance)); //Update the value on the new format
				}
				
				tracks.add(track);
				
			} catch (SecurityException e) {
				System.out.println("Failed to convert old track format! Security Manage blocked reflection!");
			} catch (NoSuchFieldException e) {
				System.out.println("Failed to convert old track format! Invalid/Incompatible!");
			} catch (IllegalArgumentException e) {
				System.out.println("Failed to convert old track format! Invalid!");
			} catch (IllegalAccessException e) {
				System.out.println("Failed to convert old track format! Invalid!");
			} catch (Exception e){
				System.out.println("Failed to convert old track format! Invalid/Incompatible!");
			}
		}
		//Now all are updated, lets save the changes
		save();
	}

	public RaceTrack getRaceTrack(String trackName) {
		for (RaceTrack track : getRaceTracks()) {
			if (track.getTrackName().equalsIgnoreCase(trackName)) {
				return track;
			}
		}
		return null;
	}

	public ArrayList<RaceTrack> getRaceTracks() {
		return new ArrayList<RaceTrack>(tracks);
	}

	public ArrayList<String> getRaceTrackNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (RaceTrack tr : getRaceTracks()) {
			names.add(tr.getTrackName());
		}
		return names;
	}

	public void setRaceTrack(RaceTrack track) {
		tracks.add(track);
		this.save();
	}

	public void setRaceTrack(String name, RaceTrack track) {
		@SuppressWarnings("unchecked")
		ArrayList<RaceTrack> Tracks = (ArrayList<RaceTrack>) tracks.clone();
		for (RaceTrack tr : Tracks) {
			if (tr.getTrackName().equalsIgnoreCase(name)) {
				tracks.remove(tr);
			}
		}
		tracks.add(track);
		this.save();
	}

	public void deleteRaceTrack(String name) {
		@SuppressWarnings("unchecked")
		ArrayList<RaceTrack> Tracks = (ArrayList<RaceTrack>) tracks.clone();
		for (RaceTrack tr : Tracks) {
			if (tr.getTrackName().equalsIgnoreCase(name)) {
				main.signManager.delQueueSigns(tr.getTrackName());
				tracks.remove(tr);
			}
		}
		save();
		return;
	}

	public Boolean raceTrackExists(String name) {
		@SuppressWarnings("unchecked")
		ArrayList<RaceTrack> Tracks = (ArrayList<RaceTrack>) tracks.clone();
		for (RaceTrack tr : Tracks) {
			if (tr.getTrackName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public void reloadTracks() {
		this.save();
		this.load();
	}

	public void revertToFile() {
		this.load();
	}
}
