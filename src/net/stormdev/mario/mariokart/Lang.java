package net.stormdev.mario.mariokart;

public class Lang {
	main plugin = null;

	public Lang(main main) {
		this.plugin = main;
	}

	public String get(String key) {
		String val = getRaw(key);
		val = main.colorise(val);
		return val;
	}

	public String getRaw(String key) {
		if (!plugin.lang.contains(key)) {
			return key;
		}
		return plugin.lang.getString(key);
	}
}
