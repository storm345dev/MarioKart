package net.stormdev.mario.signUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.tracks.RaceTrack;
import net.stormdev.mario.utils.Colors;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

public class SignManager {
	private ConcurrentHashMap<String, ArrayList<SerializableLocation>> queueSigns =
			new ConcurrentHashMap<String, ArrayList<SerializableLocation>>();
	private File queueSaveFile;
	
	public SignManager(File queueSaveFile){
		this.queueSaveFile = queueSaveFile;
		load();
	}
	
	public boolean isQueueSign(Sign sign){
		String trackName = ChatColor.stripColor(sign.getLine(0));
		if(trackName == null || !queueSigns.containsKey(trackName)){
			return false;
		}
		Location loc = sign.getLocation();
		for(SerializableLocation sloc:queueSigns.get(trackName)){
			if(sloc.getLocation(Bukkit.getServer()).equals(loc)){
				//Is a queue sign
				return true;
			}
		}
		return false; //Not a queue sign
	}
	
	public void updateSigns(){
		@SuppressWarnings("unchecked")
		List<RaceTrack> tracks = (List<RaceTrack>) MarioKart.plugin.trackManager.getRaceTracks().clone();
		for(RaceTrack t:tracks){
			updateSigns(t);
		}
		return;
	}
	
	public void clearSigns(){
		@SuppressWarnings("unchecked")
		List<RaceTrack> tracks = (List<RaceTrack>) MarioKart.plugin.trackManager.getRaceTracks().clone();
		for(RaceTrack t:tracks){
			clearSigns(t, MarioKart.plugin.getServer());
		}
		return;
	}
	
	public void updateSigns(String trackName){
		RaceTrack track = MarioKart.plugin.trackManager.getRaceTrack(trackName);
		updateSigns(track);
		return;
	}
	
	public synchronized void updateSigns(RaceTrack track){
		Server server = MarioKart.plugin.getServer();
		String name = track.getTrackName();
		ArrayList<SerializableLocation> slocs = getLocs(name);
		if(slocs.size() < 1){
			return; //No signs
		}
		
		Boolean update = false;
	    LinkedHashMap<UUID, RaceQueue> queues = MarioKart.plugin.raceQueues.getQueues(name);
	    
	    String line0 = name; //eg. [MyTrack:]
	    if(line0.length() > 15){
	    	line0 = name;
	    	if(line0.length() > 15){
	    		line0 = name.substring(15);
	    	}
	    }
	    ArrayList<String> otherLines = new ArrayList<String>();
	    int l = 0;
	    for(UUID id:queues.keySet()){
	    	if(l >= 3){ //Signs lines have 0-3
	    		continue; //Sign full
	    	} 
	    	//Signs have 15 chars per line
	    	RaceQueue queue = queues.get(id);
	    	if(queue != null){
	    		try {
					String line = MarioKart.colors.getTitle()+"["+queue.currentPlayerCount()+"/"+queue.playerLimit()+"]("
							+queue.getRaceMode().name().toLowerCase()+")";
					if(line.length() > 15){
						line = MarioKart.colors.getTitle()+"["+queue.currentPlayerCount()+"/"+queue.playerLimit()+"]";
					}
					otherLines.add(line);
				} catch (Exception e) {
					//Queue updated mid-operation
				}
		    	l++;
	    	}
	    }
		
		for(SerializableLocation sloc:new ArrayList<SerializableLocation>(slocs)){
			Location loc = sloc.getLocation(server);
			BlockState s = loc.getBlock().getState();
			if(!(s instanceof Sign)){
				update = true;
				slocs.remove(s);
				continue;
			}
			Sign sign = (Sign)s;
			sign.setLine(0, line0);
			sign.setLine(1,  otherLines.size() > 0 ? otherLines.get(0):"");
			sign.setLine(2,  otherLines.size() > 1 ? otherLines.get(1):"");
			sign.setLine(3,  otherLines.size() > 2 ? otherLines.get(2):"");
			sign.update(true);
		}
		
		if(update){
			queueSigns.put(name, slocs);
			asyncSave();
		}
		
		return;
	}
	
	public synchronized void clearSigns(RaceTrack track, Server server){
		String name = track.getTrackName();
		ArrayList<SerializableLocation> slocs = getLocs(name);
		if(slocs.size() < 1){
			return; //No signs
		}
		
		Boolean update = false;
	    
	    String line0 = name; //eg. [MyTrack:]
	    if(line0.length() > 15){
	    	line0 = name;
	    	if(line0.length() > 15){
	    		line0 = name.substring(15);
	    	}
	    }
		for(SerializableLocation sloc:new ArrayList<SerializableLocation>(slocs)){
			Location loc = sloc.getLocation(server);
			BlockState s = loc.getBlock().getState();
			if(!(s instanceof Sign)){
				update = true;
				slocs.remove(s);
				continue;
			}
			Sign sign = (Sign)s;
			sign.getLines()[0] = line0;
			sign.getLines()[1] = "";
			sign.getLines()[2] = "";
			sign.getLines()[3] = "";
			sign.update(true);
		}
		
		if(update){
			queueSigns.put(name, slocs);
			asyncSave();
		}
		
		return;
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
			for(String dest:MarioKart.plugin.trackManager.getRaceTrackNames()){
				if(ChatColor.stripColor(Colors.colorise(dest)).equalsIgnoreCase(name)){
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
		MarioKart.plugin.getServer().getScheduler().runTaskAsynchronously(MarioKart.plugin, new BukkitRunnable(){

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
