package net.stormdev.mario.utils;

import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.utils.IconMenu.OptionClickEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
		menu.setOption(8, new ItemStack(Material.EMERALD)
	        , main.colors.getTitle()+"Exit Menu", 
	        new String[]{main.colors.getInfo()+"Exit this menu!"});
		return menu;
	}
	public static void openShop(Player player){
		getShop().open(player);
		return;
	}

}
