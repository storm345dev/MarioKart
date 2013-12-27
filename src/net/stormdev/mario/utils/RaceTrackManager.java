package net.stormdev.mario.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	@SuppressWarnings("unchecked")
	public synchronized void load() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					this.saveFile));
			Object result = ois.readObject();
			ois.close();
			tracks = (ArrayList<RaceTrack>) result;
		} catch (Exception e) {
			// File just created
		}
	}

	public synchronized RaceTrack getRaceTrack(String trackName) {
		for (RaceTrack track : tracks) {
			if (track.getTrackName().equalsIgnoreCase(trackName)) {
				return track;
			}
		}
		return null;
	}

	public ArrayList<RaceTrack> getRaceTracks() {
		return new ArrayList<RaceTrack>(tracks);
	}

	public synchronized ArrayList<String> getRaceTrackNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (RaceTrack tr : tracks) {
			names.add(tr.getTrackName());
		}
		return names;
	}

	public void setRaceTrack(RaceTrack track) {
		tracks.add(track);
		this.save();
	}

	public synchronized void setRaceTrack(String name, RaceTrack track) {
		for (RaceTrack tr : new ArrayList<RaceTrack>(tracks)) {
			if (tr.getTrackName().equalsIgnoreCase(name)) {
				tracks.remove(tr);
			}
		}
		tracks.add(track);
		this.save();
	}

	public synchronized void deleteRaceTrack(String name) {
		@SuppressWarnings("unchecked")
		ArrayList<RaceTrack> Tracks = (ArrayList<RaceTrack>) tracks.clone();
		for (RaceTrack tr : Tracks) {
			if (tr.getTrackName().equalsIgnoreCase(name)) {
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
