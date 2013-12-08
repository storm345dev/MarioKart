package net.stormdev.mario.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Unlockable implements Serializable {
	private static final long serialVersionUID = 6429581539584848110L;

	public HotBarUpgrade type = HotBarUpgrade.SPEED_BOOST;
	public Map<String, Object> data = new HashMap<String, Object>();
	public int quantity = 3;
	public String upgradeName = "Speed Burst";
	public Unlockable(HotBarUpgrade type, Map<String, Object> data, int quantity, String upgradeName){
		this.type = type;
		this.data = data;
		this.upgradeName = upgradeName;
	}

}
