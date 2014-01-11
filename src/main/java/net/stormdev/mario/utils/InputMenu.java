package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class InputMenu implements Listener {

	private String name;
	private int size;
	private OptionClickEventHandler handler;
	private Plugin plugin;

	private String[] optionNames;
	private ItemStack[] optionIcons;
	private ArrayList<Integer> cancelSlots = new ArrayList<Integer>();

	public InputMenu(String name, int size, OptionClickEventHandler handler,
			Plugin plugin) {
		this.name = name;
		this.size = size;
		this.handler = handler;
		this.plugin = plugin;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void addButtonSlot(int position) {
		this.cancelSlots.add(position);
		return;
	}

	public InputMenu setOption(int position, ItemStack icon, String name,
			String... info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}

	public void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, size, name);
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
		player.openInventory(inventory);
	}

	public void destroy() {
		HandlerList.unregisterAll(this);
		handler = null;
		plugin = null;
		optionNames = null;
		optionIcons = null;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onInventoryClick(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getTitle().equals(name)) {
			int slot = event.getRawSlot();
			Boolean place = false;
			if (event.getAction() == InventoryAction.PLACE_ALL
					|| event.getAction() == InventoryAction.PLACE_ONE
					|| event.getAction() == InventoryAction.PLACE_SOME) {
				place = true;
			}
			if (cancelSlots.contains(slot) && !place) {
				event.setCancelled(true);
			}
			if (slot >= 0 && slot < size) {
				Plugin plugin = this.plugin;
				OptionClickEvent e = new OptionClickEvent(
						(Player) event.getWhoClicked(), slot,
						optionNames[slot], event.getInventory(), this);
				handler.onOptionClick(e);
				if (e.willClose()) {
					final Player p = (Player) event.getWhoClicked();
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							p.closeInventory();
						}
					}, 1l);
				}
				if (e.willDestroy()) {
					destroy();
				}
			}
		}
	}

	public interface OptionClickEventHandler {
		public void onOptionClick(OptionClickEvent event);
	}

	public class OptionClickEvent {
		private Player player;
		private int position;
		private String name;
		private boolean close;
		private boolean destroy;
		private Inventory i = null;
		private InputMenu im = null;

		public OptionClickEvent(Player player, int position, String name,
				Inventory i, InputMenu im) {
			this.player = player;
			this.position = position;
			this.name = name;
			this.close = false;
			this.destroy = false;
			this.i = i;
			this.im = im;
		}

		public InputMenu getMenu() {
			return im;
		}

		public Inventory getInventory() {
			return i;
		}

		public Player getPlayer() {
			return player;
		}

		public int getPosition() {
			return position;
		}

		public String getName() {
			return name;
		}

		public boolean willClose() {
			return close;
		}

		public boolean willDestroy() {
			return destroy;
		}

		public void setWillClose(boolean close) {
			this.close = close;
		}

		public void setWillDestroy(boolean destroy) {
			this.destroy = destroy;
		}
	}

	private ItemStack setItemNameAndLore(ItemStack item, String name,
			String[] lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}

}
