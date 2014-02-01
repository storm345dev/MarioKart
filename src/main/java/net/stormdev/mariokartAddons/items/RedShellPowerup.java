package net.stormdev.mariokartAddons.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.User;
import net.stormdev.mario.mariokart.main;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.ucars.ItemStackFromId;

public class RedShellPowerup extends TrackingShellPowerup {
	
	public RedShellPowerup(){
		super.setItemStack(getBaseItem());
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}
	
	public static PowerupType getPowerupType(){
		return PowerupType.RED_SHELL;
	}
	
	private static final ItemStack getBaseItem(){
		String id = main.config.getString("mariokart.redShell");
		ItemStack i = ItemStackFromId.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add(main.colors.getInfo()+"+Slows down the victim");
		lore.add(main.colors.getInfo()+"*Right click to deploy");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(main.colors.getInfo()+"Red Shell");
		i.setItemMeta(im);
		
		return i;
	}

	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		SortedMap<String, Double> sorted = race.getRaceOrder();
		Set<String> keys = sorted.keySet();
		Object[] pls = keys.toArray();
		int ppos = 0;
		for (int i = 0; i < pls.length; i++) {
			if (pls[i].equals(player.getName())) {
				ppos = i;
			}
		}
		int tpos = ppos - 1;
		if (tpos < 0) {
			tpos = ppos + 1;
			if (tpos < 0 || tpos >= pls.length) {
				return;
			}
		}
		
		setTarget((String) pls[tpos]);
		inHand.setAmount(inHand.getAmount() - 1);
		spawn(carLoc, player);
		start(); //Start tracking target player
	}

	@Override
	public PowerupType getType() {
		return PowerupType.RED_SHELL;
	}
	
}
