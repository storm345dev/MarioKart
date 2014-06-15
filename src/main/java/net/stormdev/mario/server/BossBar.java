package net.stormdev.mario.server;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BossBar {
	
	private static boolean a(){
		
		boolean found = Bukkit.getPluginManager().getPlugin("BarAPI")!=null;
		try {
			Class.forName("me.confuser.barapi.BarAPI");
			found = true;
		} catch (ClassNotFoundException e) {
			found = false;
		}
		return !found; //Return true if no BarAPI
	}
	
	public static void setMessage(Player player, String message){
		if(a()){return;};
		BarAPI.setMessage(player, message);
	}
	
	public static void setMessage(Player player, String message, float percent){
		if(a()){return;};
		BarAPI.setMessage(player, message, percent);
	}
	
	public static void setMessage(final Player player, String message, int seconds){
		if(a()){return;};
		BarAPI.setMessage(player, message, seconds);
	}
	
	public static boolean hasBar(Player player){
		if(a()){return true;};
		return BarAPI.hasBar(player);
	}
	
	public static void removeBar(Player player){
		if(a()){return;};
		BarAPI.removeBar(player);
	}
	
	public static void setHealth(Player player, float percent){
		if(a()){return;};
		BarAPI.setHealth(player, percent);
	}
}
