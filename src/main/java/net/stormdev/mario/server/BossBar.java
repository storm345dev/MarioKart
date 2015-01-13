package net.stormdev.mario.server;

import me.confuser.barapi.BarAPI;
import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/*
 * Include BarAPI from 1.8_BarAPI with refactored packaged and if not found, use that
 * 
 */
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
	
	private static void execAsync(Runnable run){
		if(Bukkit.isPrimaryThread()){
			run.run();
		}
		else {
			Bukkit.getScheduler().runTask(MarioKart.plugin, run);
		}
	}
	
	public static void setMessage(final Player player, final String message){
		if(a()){
			execAsync(new Runnable(){

				@Override
				public void run() {
					net.stormdev.barapi_1_8.BarAPI.setMessage(player, message);
					return;
				}});
			return;
		}
		
		execAsync(new Runnable(){

			@Override
			public void run() {
				BarAPI.setMessage(player, message);
				return;
			}});
	}
	
	public static void setMessage(final Player player, final String message, final float percent){
		if(a()){
			execAsync(new Runnable(){

				@Override
				public void run() {
					net.stormdev.barapi_1_8.BarAPI.setMessage(player, message, percent);
					return;
				}});
			return;
		}
		
		execAsync(new Runnable(){

			@Override
			public void run() {
				BarAPI.setMessage(player, message, percent);
				return;
			}});
	}
	
	public static void setMessage(final Player player, final String message, final int seconds){
		if(a()){
			execAsync(new Runnable(){

				@Override
				public void run() {
					net.stormdev.barapi_1_8.BarAPI.setMessage(player, message, seconds);
					return;
				}});
			return;
		}

		execAsync(new Runnable(){

			@Override
			public void run() {
				BarAPI.setMessage(player, message, seconds);
				return;
			}});
	}
	
	public static boolean hasBar(Player player){
		if(a()){return net.stormdev.barapi_1_8.BarAPI.hasBar(player);};
		return BarAPI.hasBar(player);
	}
	
	public static void removeBar(final Player player){
		if(a()){
			execAsync(new Runnable(){

				@Override
				public void run() {
					net.stormdev.barapi_1_8.BarAPI.removeBar(player);
					return;
				}});
			return;
		}
		
		execAsync(new Runnable(){

			@Override
			public void run() {
				BarAPI.removeBar(player);
				return;
			}});
		
	}
	
	public static void setHealth(final Player player, final float percent){
		if(a()){
			execAsync(new Runnable(){

				@Override
				public void run() {
					net.stormdev.barapi_1_8.BarAPI.setHealth(player, percent);
					return;
				}});
			return;
		}
		
		execAsync(new Runnable(){

			@Override
			public void run() {
				BarAPI.setHealth(player, percent);
				return;
			}});
	}
}
