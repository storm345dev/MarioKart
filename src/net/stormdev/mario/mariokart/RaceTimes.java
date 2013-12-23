package net.stormdev.mario.mariokart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.stormdev.mario.utils.LowHighDoubleValueComparator;

public class RaceTimes {
	public File saveFile = null;
	public ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> times = new ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>();
	public Boolean saved = true;

	public RaceTimes(File saveFile, Boolean saved) {
		this.saveFile = saveFile;
		this.saveFile.getParentFile().mkdirs();
		if (this.saveFile.length() < 1 || !this.saveFile.exists()) {
			try {
				this.saveFile.createNewFile();
			} catch (IOException e) {
			}
			save();
		}
		times = load(this.saveFile.getAbsolutePath());
		this.saved = saved;
	}

	public void addRaceTime(String trackName, String playerName, double time) {
		if (saved) {
			ConcurrentHashMap<String, Double> scores = new ConcurrentHashMap<String, Double>();
			scores = getTimes(trackName);
			Boolean prev = false;
			double previous = Double.MAX_VALUE;
			if (scores.containsKey(playerName)) {
				prev = true;
				previous = scores.get(playerName);
			}
			if (time < previous || !prev) {
				scores.put(playerName, time);
			}
			times.put(trackName, scores);
			save();
			return;
		}
		return;
	}

	public void clearRaceTimes(String trackName) {
		times.remove(trackName);
		return;
	}

	public SortedMap<String, Double> getTopTimes(double topManyCount,
			String trackName) {
		ConcurrentMap<String, Double> t = new ConcurrentHashMap<String, Double>();
		t = getTimes(trackName);
		LowHighDoubleValueComparator com = new LowHighDoubleValueComparator(t);
		SortedMap<String, Double> sorted = new TreeMap<String, Double>(com);
		sorted.putAll(t);
		return sorted;
	}

	public ConcurrentHashMap<String, Double> getTimes(String trackName) {
		ConcurrentHashMap<String, Double> t = new ConcurrentHashMap<String, Double>();
		if (times.containsKey(trackName)) {
			t = times.get(trackName);
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	public static ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> load(String path) {
		try {
			System.out.println("Loading information!");
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					path));
			Object result = ois.readObject();
			ois.close();
			try {
				return (ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>) result;
			} catch (Exception e) {
				ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> h = new ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>();
				HashMap<String, HashMap<String, Double>> o = ((HashMap<String, HashMap<String, Double>>)result);
				for(String k:o.keySet()){
					HashMap<String, Double> oo = o.get(k);
					ConcurrentHashMap<String, Double> hh = new ConcurrentHashMap<String, Double>();
					hh.putAll(oo);
					h.put(k, hh);
				}
			    return h;
			}
		} catch (Exception e) {
			System.out.println("Information failed to load error:");
			e.printStackTrace();
			return null;
		}
	}

	public void save() {
		save(this.times, this.saveFile.getAbsolutePath());
	}

	public static void save(ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> map,
			String path) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			// Handle I/O exceptions
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
