package net.stormdev.mariokartAddons.items;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.items.ItemStacks;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.useful.ucarsCommon.StatValue;

public class BombPowerup extends PowerupBase {
	
	public BombPowerup(){
		super.setItemStack(getBaseItem());
	}

	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		inHand.setAmount(inHand.getAmount() - 1);
		final Vector vel = player.getEyeLocation().getDirection();
		final TNTPrimed tnt = (TNTPrimed) car.getLocation().getWorld()
				.spawnEntity(car.getLocation(), EntityType.PRIMED_TNT);
		tnt.setFuseTicks(80);
		tnt.setMetadata("explosion.none", new StatValue(null, main.plugin));
		vel.setY(0.2); // Distance to throw it
		tnt.setVelocity(vel);
		main.plugin.getServer().getScheduler()
				.runTaskAsynchronously(main.plugin, new Runnable() {
					@Override
					public void run() {
						int count = 12;
						if (count > 0) {
							count--;
							tnt.setVelocity(vel);
							tnt.setMetadata("explosion.none",
									new StatValue(null, main.plugin));
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
							}
						} else {
							return;
						}
					}
				});
	}

	@Override
	public void doLeftClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		return; //Do nothing
	}
	
	private static final ItemStack getBaseItem(){
		String id = main.config.getString("mariokart.bomb");
		ItemStack i = ItemStacks.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Throws an ignited bomb");
		lore.add("*Right click to deploy");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(main.colors.getInfo()+"Bomb");
		i.setItemMeta(im);
		
		return i;
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}

	@Override
	public PowerupType getType() {
		return PowerupType.BOMB;
	}
	
	public static PowerupType getPowerupType() {
		return PowerupType.BOMB;
	}

}
