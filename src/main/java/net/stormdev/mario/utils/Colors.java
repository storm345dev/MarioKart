package net.stormdev.mario.utils;

import org.bukkit.ChatColor;

public class Colors {
	private String success = "";
	private String error = "";
	private String info = "";
	private String title = "";
	private String tp = "";

	public Colors(String success, String error, String info, String title,
			String tp) {
		this.success = colorise(success);
		this.error = colorise(error);
		this.info = colorise(info);
		this.title = colorise(title);
		this.tp = colorise(tp);
	}

	public String getSuccess() {
		return this.success;
	}

	public String getError() {
		return this.error;
	}

	public String getInfo() {
		return this.info;
	}

	public String getTitle() {
		return this.title;
	}

	public String getTp() {
		return this.tp;
	}
	
	public static String colorise(String prefix) {
		return ChatColor.translateAlternateColorCodes('&', prefix);
	}
}
