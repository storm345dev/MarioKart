package net.stormdev.mario.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class HotBarItem {
	private ItemStack displayItem = null;
	private String displayName = "";
	public String shortId = "";
	private int quantity = 1;
	private HotBarUpgrade type = HotBarUpgrade.SPEED_BOOST;
	Map<String, Object> data = new HashMap<String, Object>();

	public HotBarItem(ItemStack displayItem, String displayName, int quantity,
			HotBarUpgrade type, Map<String, Object> data, String unlockId) {
		this.displayItem = displayItem;
		this.displayName = displayName;
		this.quantity = quantity;
		this.type = type;
		this.data = data;
		this.shortId = unlockId;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
		return;
	}

	public ItemStack getDisplayItem() {
		return this.displayItem;
	}

	public void setDisplayItem(ItemStack item) {
		this.displayItem = item;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String name) {
		this.displayName = name;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int amount) {
		this.quantity = amount;
	}

	public HotBarUpgrade getType() {
		return this.type;
	}

	public void setType(HotBarUpgrade type) {
		this.type = type;
	}

}
