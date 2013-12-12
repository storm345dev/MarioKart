package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.utils.IconMenu.OptionClickEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Shop {
	
	final public static IconMenu getShop(){
		final IconMenu menu = new IconMenu(main.colors.getTitle()+"MarioKart Shop", 
				9, new IconMenu.OptionClickEventHandler(){

					@Override
					public void onOptionClick(OptionClickEvent event) {
						SelectMenuClickEvent evt = new SelectMenuClickEvent(event, 
								SelectMenuType.MENU, 1);
						main.plugin.getServer().getPluginManager().callEvent(evt);
						if(evt.isCancelled()){
							event.setWillClose(false);
							event.setWillDestroy(false);
							return;
						}
						event = evt.getClickEvent();
					}}, main.plugin);
		menu.setOption(0, new ItemStack(Material.EMERALD)
		    , main.colors.getTitle()+"Buy Upgrades", 
		    new String[]{main.colors.getInfo()+"Upgrade your Kart!"});
		menu.setOption(1, new ItemStack(Material.EMERALD)
	        , main.colors.getTitle()+"Sell Upgrades", 
	        new String[]{main.colors.getInfo()+"Remove Kart upgrades!"});
		menu.setOption(8, new ItemStack(Material.WOODEN_DOOR)
	        , main.colors.getTitle()+"Exit Menu", 
	        new String[]{main.colors.getInfo()+"Exit this menu!"});
		return menu;
	}
	public static void openShop(Player player){
		getShop().open(player);
		return;
	}
	public static void openUpgradeShop(Player player, int page){
		getUpgradesForSaleMenu(page).open(player);
	}
	public static IconMenu getUpgradesForSaleMenu(final int page){
		String title = main.colors.getTitle()+"Buy Upgrades Page: "+page;
		if(title.length() > 32){
			title = main.colors.getError()+"Buy Upgrades (ERROR:Too Long)";
		}
		final Map<String, Unlockable> unlocks = new HashMap<String, Unlockable>(main.plugin.getUnlocks());
		final IconMenu menu = new IconMenu(title, 54, new IconMenu.OptionClickEventHandler() {
            public void onOptionClick(IconMenu.OptionClickEvent event) {
            	event.setWillClose(true);
            	event.setWillDestroy(true);
            	SelectMenuClickEvent evt = new SelectMenuClickEvent(event, 
						SelectMenuType.BUY_UPGRADES, page);
				main.plugin.getServer().getPluginManager().callEvent(evt);
				if(evt.isCancelled()){
					event.setWillClose(false);
					event.setWillDestroy(false);
					return;
				}
				event = evt.getClickEvent();
            }
        }, main.plugin);
		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
		menu.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
		menu.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
		//Set option slots for all upgrades for sale
		//1-51 slots available on the page
		int pos = 1;
		int arrayStartPos = (page-1)*51;
		final Object[] keys = unlocks.keySet().toArray();
		for(int i=arrayStartPos;i<(arrayStartPos+52)&&i<unlocks.size();i++){
			if(pos<52){
				Unlockable unlock = unlocks.get(keys[i]);
				ItemStack display = new ItemStack(unlock.displayItem);
				ArrayList<String> lore = new ArrayList<String>();
				lore.add(main.colors.getInfo()+"Effect: "+unlock.type.name().toLowerCase());
				lore.add(main.colors.getInfo()+"Price: "+unlock.price
						+" "+main.config.getString("general.race.rewards.currency"));
				menu.setOption(pos, display, main.colors.getTitle()+unlock.upgradeName, lore);
				pos++;
			}
		}
		return menu;
	}

}
