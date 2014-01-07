package net.stormdev.mario.signUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitRunnable;

public class SignManager {
	private ConcurrentHashMap<String, ArrayList<SerializableLocation>> queueSigns =
			new ConcurrentHashMap<String, ArrayList<SerializableLocation>>();
	private File queueSaveFile;
	
	public SignManager(File queueSaveFile){
		this.queueSaveFile = queueSaveFile;
		load();
	}
	
	public List<SerializableLocation> getQueueSigns(String trackName, Server server){
		List<SerializableLocation> sl = queueSigns.get(getCorrectName(trackName));
		return sl;
	}
	
	public Boolean queueSignsExistsFor(String trackName){
		return queueSigns.containsKey(getCorrectName(trackName));
	}
	
	public List<String> getqueueSignsTrackList(){
		return new ArrayList<String>(queueSigns.keySet());
	}
	
	public ConcurrentHashMap<String, ArrayList<SerializableLocation>> getqueueSigns(){
		return queueSigns;
	}
	
	public String getCorrectName(String name){
		if(!queueSigns.containsKey(name)){
			for(String dest:main.plugin.trackManager.getRaceTrackNames()){
				if(ChatColor.stripColor(main.colorise(dest)).equalsIgnoreCase(name)){
					name = dest;
				}
			}
		}
		return name;
	}
	
	public synchronized void addQueueSign(String name, Location loc){
		name = getCorrectName(name);
		ArrayList<SerializableLocation> slocs = getLocs(name);
		slocs.add(new SerializableLocation(loc));
		queueSigns.put(name, slocs);
		asyncSave();
		return;
	}
	
	public ArrayList<SerializableLocation> getLocs(String name){
		ArrayList<SerializableLocation> slocs = new ArrayList<SerializableLocation>();
		if(queueSignsExistsFor(name)){
			slocs = queueSigns.get(name);
		}
		return slocs;
	}
	
	public synchronized void delQueueSigns(String name){
		name = getCorrectName(name);
		if(!queueSigns.containsKey(name)){
			return;
		}
		queueSigns.remove(name);
		asyncSave();
		return;
	}
	
	public synchronized void delQueueSign(String track, SerializableLocation sloc){
		track = getCorrectName(track);
		ArrayList<SerializableLocation> slocs = getLocs(track);
		if(!slocs.contains(sloc)){
			return;
		}
		slocs.remove(sloc);
		queueSigns.put(track, slocs);
		asyncSave();
		return;
	}
	
	public void asyncSave(){
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			public void run() {
				save();
				return;
			}});
		return;
	}
	public void load(){
		this.queueSaveFile.getParentFile().mkdirs();
		if(!this.queueSaveFile.exists() || this.queueSaveFile.length() < 1){
			try {
				this.queueSaveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		else{
			try {
				this.queueSigns = loadHashMap(this.queueSaveFile.getAbsolutePath());
			} catch (Exception e) {
				//Old format
				this.queueSigns = null;
			}
		}
		if(this.queueSigns == null){
			this.queueSigns = new ConcurrentHashMap<String, ArrayList<SerializableLocation>>();
		}
	}
	private void save(){
		this.queueSaveFile.getParentFile().mkdirs();
		if(!this.queueSaveFile.exists() || this.queueSaveFile.length() < 1){
			try {
				this.queueSaveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		saveHashMap(queueSigns, this.queueSaveFile.getAbsolutePath());
	}
	public static void saveHashMap(ConcurrentHashMap<String, ArrayList<SerializableLocation>> map, String path)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			//Handle I/O exceptions
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public static ConcurrentHashMap<String, ArrayList<SerializableLocation>> loadHashMap(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			try {
				return (ConcurrentHashMap<String, ArrayList<SerializableLocation>>) result;
			} catch (Exception e) {
				return new ConcurrentHashMap<String, ArrayList<SerializableLocation>>();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
