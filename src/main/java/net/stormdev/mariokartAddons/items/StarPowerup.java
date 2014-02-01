package net.stormdev.mariokartAddons.items;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.mario.items.ItemStacks;
import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.sound.MarioKartSound;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class StarPowerup extends PowerupBase {
	
	public StarPowerup(){
		super.setItemStack(getBaseItem());
	}

	@Override
	public void doRightClickAction(User user, final Player player, final Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		inHand.setAmount(inHand.getAmount()-1);
		car.setMetadata("kart.immune",
				new StatValue(15000, main.plugin)); // Value =
													// length(millis)
		player.setMetadata("kart.immune",
				new StatValue(15000, main.plugin));
		final String pname = player.getName();
		main.plugin.getServer().getScheduler()
				.runTaskLater(main.plugin, new Runnable() {

					@Override
					public void run() {
						Player pl = main.plugin.getServer().getPlayer(
								pname);
						if (pl != null) {
							pl.removeMetadata("kart.immune", main.plugin);
							car.removeMetadata("kart.immune",
									main.plugin);
						}
					}
				}, 300l);
		main.plugin.getServer().getScheduler()
				.runTaskAsynchronously(main.plugin, new Runnable() {

					@Override
					public void run() {
						int amount = 5;
						while (amount > 0) {
							if (ucars.listener.inACar(player)) {
								if(!main.plugin.playCustomSound(player, MarioKartSound.STAR_RIFF)){
									player.getLocation()
									.getWorld()
									.playSound(
											player.getLocation(),
											Sound.BURP, 3, 1);
								}
							}
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
							}
							amount--;
						}
						return;
					}
				});
		ucars.listener.carBoost(player.getName(), 35, 15000,
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
		String id = main.config.getString("mariokart.star");
		ItemStack i = ItemStacks.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Applies a large speed boost");
		lore.add("+Immunity to other powerups");
		lore.add("*Right click to use");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(main.colors.getInfo()+"Star");
		i.setItemMeta(im);
		
		return i;
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}

	@Override
	public PowerupType getType() {
		return PowerupType.STAR;
	}
	
	public static PowerupType getPowerupType() {
		return PowerupType.STAR;
	}

}
