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

public class BananaPowerup extends PowerupBase {
	
	public BananaPowerup(){
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
		Location loc = player.getLocation().add(
				player.getEyeLocation().getDirection().multiply(-1));
		loc.getWorld().dropItem(loc, getNewItem());
		inHand.setAmount(inHand.getAmount()-1);
	}

	@Override
	public void doLeftClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		return; //Do nothing
	}
	
	private static final ItemStack getBaseItem(){
		String id = main.config.getString("mariokart.banana");
		ItemStack i = ItemStacks.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Slows players down");
		lore.add("*Right click to deploy");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(main.colors.getInfo()+"Banana");
		i.setItemMeta(im);
		
		return i;
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}

	@Override
	public PowerupType getType() {
		return PowerupType.BANANA;
	}
	
	public static PowerupType getPowerupType() {
		return PowerupType.BANANA;
	}

}
