package net.stormdev.mario.mariokart;

import net.stormdev.mario.utils.Colors;

public class Lang {
	MarioKart plugin = null;

	public Lang(MarioKart main) {
		this.plugin = main;
	}

	public String get(String key) {
		String val = getRaw(key);
		val = Colors.colorise(val);
		return val;
	}

	public String getRaw(String key) {
		if (!plugin.lang.contains(key)) {
			return key;
		}
		return plugin.lang.getString(key);
	}
}
