package net.stormdev.mariokartAddons;

import org.bukkit.inventory.ItemStack;

import com.useful.ucars.ItemStackFromId;

import net.stormdev.ucars.race.main;

public class PowerupMaker {
	public static PowerupData getPowerupRaw(Powerup powerup, int amount){
		String power = "40";
   	    if(powerup == Powerup.BANANA){
   		    power = main.config.getString("mariokart.banana");
   	    }
   	    else if(powerup == Powerup.BLUE_SHELL){
   	    	power = main.config.getString("mariokart.blueShell");
   	    }
   	    else if(powerup == Powerup.BOMB){
	    	power = main.config.getString("mariokart.bomb");
	    }
   	    else if(powerup == Powerup.GREEN_SHELL){
	    	power = main.config.getString("mariokart.greenShell");
	    }
   	    else if(powerup == Powerup.LIGHTNING){
	    	power = main.config.getString("mariokart.lightning");
	    }
   	    else if(powerup == Powerup.MUSHROOM){
	    	power = main.config.getString("mariokart.mushroom");
	    }
   	    else if(powerup == Powerup.POW){
	    	power = main.config.getString("mariokart.pow");
	    }
   	    else if(powerup == Powerup.RED_SHELL){
	    	power = main.config.getString("mariokart.redShell");
	    }
   	    else if(powerup == Powerup.STAR){
	    	power = main.config.getString("mariokart.star");
	    }
   	    else if(powerup == Powerup.RANDOM){
	    	power = main.config.getString("mariokart.random");
	    }
   	    else if(powerup == Powerup.BOO){
   	    	power = main.config.getString("mariokart.boo");
   	    }
   	    PowerupData toReturn = new PowerupData(powerup, ItemStackFromId.get(power));
   	    toReturn.raw.setAmount(amount);
   	    return toReturn;
	}
	public static ItemStack getPowerup(Powerup powerup, int amount){
		PowerupData data = PowerupMaker.getPowerupRaw(powerup, amount);
		PowerupItem item = new PowerupItem(data);
		return item;
	}

}
