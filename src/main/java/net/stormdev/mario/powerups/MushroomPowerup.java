package net.stormdev.mario.powerups;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.items.ItemStacks;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.ucars.ucars;

public class MushroomPowerup extends PowerupBase {
	
	public MushroomPowerup(){
		super.setItemStack(getBaseItem());
	}
	
	@Override
	public ItemStack getNewItem(){
		//Shells can be between 1 and 3 in quantity
				ItemStack s = super.stack.clone();
				
				int rand = main.plugin.random.nextInt(6); //Between 0 and 5
				rand -= 2; //Between -2 and 3
				if(rand < 1)
					rand = 1;
				
				s.setAmount(rand);
				
				return s;
	}

	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		inHand.setAmount(inHand.getAmount() - 1);
		ucars.listener.carBoost(player.getName(), 19, 9000,
				ucars.config.getDouble("general.cars.defSpeed")); // Apply
																	// speed
																	// boost
	}

	@Override
	public void doLeftClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		return; //Do nothing
	}
	
	private static final ItemStack getBaseItem(){
		String id = main.config.getString("mariokart.mushroom");
		ItemStack i = ItemStacks.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Applies a short speed boost");
		lore.add("*Right click to use");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(main.colors.getInfo()+"Mushroom");
		i.setItemMeta(im);
		
		return i;
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}

	@Override
	public PowerupType getType() {
		return PowerupType.MUSHROOM;
	}
	
	public static PowerupType getPowerupType() {
		return PowerupType.MUSHROOM;
	}

}
