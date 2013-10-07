package net.stormdev.mariokartAddons;


import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.race.main;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PowerupItem extends ItemStack{
     public PowerupItem(PowerupData powerupRaw){
    	 super(powerupRaw.raw.getTypeId());
    	 this.setDurability(powerupRaw.raw.getDurability());
    	 this.setAmount(powerupRaw.raw.getAmount());
    	 Powerup powerup = powerupRaw.powerup;
    	 String pow = powerup.toString().toLowerCase();
    	 if(pow.length() > 1){
    	 String body = pow.substring(1);
    	 String start = pow.substring(0, 1);
    	 pow = start.toUpperCase()+body;
    	 }
    	 ItemMeta meta= this.getItemMeta();
    	 meta.setDisplayName(main.colors.getInfo()+pow);
    	 //Set lore based on Item
    	 List<String> lore = new ArrayList<String>();
    	 if(powerup==Powerup.BANANA){
    		 lore.add("+Slows players down");
    		 lore.add("*Right click to deploy");
    	 }
    	 else if(powerup==Powerup.BLUE_SHELL){
    		 lore.add("+Targets and slows the leader");
    		 lore.add("*Right click to deploy");
    	 }
    	 else if(powerup==Powerup.BOMB){
    		 lore.add("+Throws an ignited bomb");
    		 lore.add("*Right click to deploy");
    	 }
    	 else if(powerup==Powerup.GREEN_SHELL){
    		 lore.add("+Slows down the victim");
    		 lore.add("*Left click to throw forwards");
    		 lore.add("*Right click to throw backwards");
    	 }
    	 else if(powerup==Powerup.LIGHTNING){
    		 lore.add("+Strikes all lightning on enemies");
    		 lore.add("*Right click to deploy");
    	 }
    	 else if(powerup==Powerup.MUSHROOM){
    		 lore.add("+Applies a short speed boost");
    		 lore.add("*Right click to use");
    	 }
    	 else if(powerup==Powerup.POW){
    		 lore.add("+Freezes other players");
    		 lore.add("*Right click to deploy");
    	 }
    	 else if(powerup==Powerup.RED_SHELL){
    		 lore.add("+Slows down the victim");
    		 lore.add("*Right click to deploy");
    	 }
    	 else if(powerup==Powerup.STAR){
    		 lore.add("+Applies a large speed boost");
    		 lore.add("+Immunity to other powerups");
    		 lore.add("*Right click to use");
    	 }
    	 else if(powerup==Powerup.RANDOM){
    		 lore.add("+Gives a random powerup");
    		 lore.add("*Right click to use");
    	 }
    	 meta.setLore(lore);
    	 this.setItemMeta(meta);
     }
}
