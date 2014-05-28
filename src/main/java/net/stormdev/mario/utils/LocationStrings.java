package net.stormdev.mario.utils;

import java.util.logging.Level;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationStrings {
	public static String getLocationString(Location loc){
		String str = "";
		str += loc.getWorld().getName()+":";
		str += loc.getX()+",";
		str += loc.getY()+",";
		str += loc.getZ()+":";
		str += loc.getPitch()+",";
		str += loc.getYaw();
		
		return str;
	}
	
	public static Location getLocation(String str){
		Location loc;
		try {
			String[] locSects = str.split(Pattern.quote(":"));
			String wName = locSects[0];
			String[] coords = locSects[1].split(Pattern.quote(","));
			double x = Double.parseDouble(coords[0]);
			double y = Double.parseDouble(coords[1]);
			double z = Double.parseDouble(coords[2]);
			
			loc = new Location(Bukkit.getServer().getWorld(wName), x, y, z);
			
			if(locSects.length > 2){
				String[] dir = locSects[2].split(Pattern.quote(","));
				float pitch = Float.parseFloat(dir[0]);
				float yaw = Float.parseFloat(dir[1]);
				loc.setPitch(pitch);
				loc.setYaw(yaw);
			}
		} catch (Exception e) {
			MarioKart.plugin.getLogger().log(Level.SEVERE, "Invalid configured location: "+str+"!");
			
			return null;
		}
		
		return loc;
	}
}
