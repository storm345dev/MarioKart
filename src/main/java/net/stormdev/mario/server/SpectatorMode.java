package net.stormdev.mario.server;

import java.util.Arrays;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.utils.MetaValue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpectatorMode implements Listener {
	private static final String META = "MarioKart.spectating";
	private static final ItemStack item;
	
	static {
		item = new ItemStack(Material.BOOK);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(ChatColor.DARK_RED+"Exit to lobby");
		im.setLore(Arrays.asList(new String[]{ChatColor.GRAY+"Right click to use"}));
		
		item.setItemMeta(im);
	}
	
	public SpectatorMode(){
		Bukkit.getPluginManager().registerEvents(this, MarioKart.plugin);
	}
	
	public void add(final Player player){
		if(isSpectating(player)){
			return;
		}
		player.setMetadata(META, new MetaValue(true, MarioKart.plugin));
		player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"Info: "+ChatColor.GOLD+"You are now spectating, to go back to the lobby at anytime; do '/race quit'.");
		player.sendMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		player.closeInventory();
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
		hide(player);
		player.setAllowFlight(true);
		spectateInv(player);
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				player.setGameMode(GameMode.SPECTATOR);
				spectateInv(player);
				return;
			}}, 12l);
		
	}
	
	private void hide(Player player){
		try {
			for(Player p:Bukkit.getOnlinePlayers()){
				p.hidePlayer(player);
			}
		} catch (Exception e) {
			//Oh well
		}
	}
	
	private void show(Player player){
		try {
			for(Player p:Bukkit.getOnlinePlayers()){
				p.showPlayer(player);
			}
		} catch (Exception e) {
			//Oh well
		}
	}
	
	public void endSpectating(){
		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player:players){
			if(isSpectating(player)){
				stopSpectating(player);
			}
		}
	}
	
	public void stopSpectating(final Player player){
		Bukkit.getScheduler().runTask(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				player.getInventory().clear();
				player.removePotionEffect(PotionEffectType.INVISIBILITY);
				show(player);
				player.removeMetadata(META, MarioKart.plugin);
				player.setAllowFlight(false);
				player.setGameMode(GameMode.SURVIVAL);
				Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

					@Override
					public void run() {
						player.teleport(FullServerManager.get().lobbyLoc);
						return;
					}}, 5l);
				return;
			}});
	}
	
	public boolean isSpectating(Player player){
		return player.hasMetadata(META);
	}
	
	@SuppressWarnings("deprecation")
	private void spectateInv(final Player player){
		/*Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				if(!isSpectating(player)){
					return;
				}
				player.getInventory().clear();
				Inventory inv = player.getInventory();
				inv.setItem(0, item.clone());
				player.updateInventory();
				return;
			}}, 5l);*/
	}
	
	@EventHandler
	void onJoin(PlayerJoinEvent event){
		if(isSpectating(event.getPlayer())){
			stopSpectating(event.getPlayer());
		}
	}
	
	/*@EventHandler
	void useExit(PlayerInteractEvent event){
		Player player = event.getPlayer();
		ItemStack inHand = player.getItemInHand();
		if(inHand == null || !(inHand.isSimilar(item))){
			return;
		}
		if(!isSpectating(player)){
			return;
		}
		player.getInventory().clear();
		stopSpectating(player);
		player.teleport(FullServerManager.get().lobbyLoc); //For when they next login
		player.sendMessage(ChatColor.GRAY+"Teleporting...");
		FullServerManager.get().sendToLobby(player);
	}*/
	
	/*@EventHandler
	void dropItem(PlayerDropItemEvent event){
		if(isSpectating(event.getPlayer())){
			event.setCancelled(true);
		}
	}*/
	
	/*@EventHandler
	void invClick(InventoryClickEvent event){
		Entity e = event.getWhoClicked();
		if(!(e instanceof Player)){
			return;
		}
		
		Player player = (Player) e;
		if(isSpectating(player)){
			event.setCancelled(true);
		}
	}*/
}
