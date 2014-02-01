package net.stormdev.mariokartAddons.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.RaceExecutor;
import net.stormdev.mario.mariokart.User;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.utils.ItemStackFromId;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.ucars.ucars;

public class LightningPowerup extends PowerupBase {
	
	public LightningPowerup(){
		super.setItemStack(getBaseItem());
	}

	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		SortedMap<String, Double> sorted = race.getRaceOrder();
		Set<String> keys = sorted.keySet();
		double Cur = ucars.config.getDouble("general.cars.defSpeed");
		double desired = 10;
		double power = Cur - desired;
		if (power < 0) {
			power = 0;
		}
		power = -power;
		for (String name:keys) {
			Player pla = main.plugin.getServer().getPlayer(
					name);
			if(!name.equals(player.getName())
					&& !main.marioKart.isPlayerImmune(pla)){
				Entity c = pla.getVehicle();
				while(c!=null && !(c instanceof Minecart) && c.getVehicle() != null){
					c = c.getVehicle();
				}
				if(!(c instanceof Minecart)){
					c = null;
				}
				Minecart cart = (Minecart) c;
				pla.getWorld().strikeLightningEffect(pla.getLocation());
				if(cart != null){
					RaceExecutor.penalty(pla,
							cart, (long) 1);
				}
				ucars.listener
						.carBoost(
								pla.getName(),
								power,
								8000,
								ucars.config
										.getDouble("general.cars.defSpeed"));
			}
	    }
		inHand.setAmount(inHand.getAmount() - 1);
	}

	@Override
	public void doLeftClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		return; //Do nothing
	}
	
	private static final ItemStack getBaseItem(){
		String id = main.config.getString("mariokart.lightning");
		ItemStack i = ItemStackFromId.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Strikes all lightning on enemies");
		lore.add("*Right click to deploy");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(main.colors.getInfo()+"Lightning");
		i.setItemMeta(im);
		
		return i;
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}

	@Override
	public PowerupType getType() {
		return PowerupType.LIGHTNING;
	}
	
	public static PowerupType getPowerupType() {
		return PowerupType.LIGHTNING;
	}

}
