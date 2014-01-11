package net.stormdev.mario.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class Unlockable implements Serializable {
	private static final long serialVersionUID = 6429581539584848110L;

	public HotBarUpgrade type = HotBarUpgrade.SPEED_BOOST;
	public Map<String, Object> data = new HashMap<String, Object>();
	public double price = 3.0;
	public String upgradeName = "Speed Burst";
	public String shortId = "";
	public Material displayItem = null;

	public Unlockable(HotBarUpgrade type, Map<String, Object> data,
			double price, String upgradeName, String shortId,
			Material displayItem) {
		this.type = type;
		this.data = data;
		this.upgradeName = upgradeName;
		this.price = price;
		this.shortId = shortId;
		this.displayItem = displayItem;
	}

}
