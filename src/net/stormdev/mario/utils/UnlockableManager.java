package net.stormdev.mario.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.stormdev.mario.mariokart.main;

public class UnlockableManager {
	
	private Map<String, Map<String, Unlockable>> data = new HashMap<String, Map<String, Unlockable>>();
	private File saveFile = null;
	private Boolean sql = false;
	private SQLManager sqlManager = null;
	public UnlockableManager(File saveFile, Boolean sql){
		this.saveFile = saveFile;
		this.sql = sql;
		if(sql){
			try {
				sqlManager = new SQLManager(main.config.getString("general.upgrades.sqlHostName"), 
						main.config.getString("general.upgrades.sqlPort"), 
						main.config.getString("general.upgrades.sqlDataBaseName"), 
						main.config.getString("general.upgrades.sqlUsername"), 
						main.config.getString("general.upgrades.sqlPassword"));
			} catch (Exception e) {
				sql = false;
			}
			if(sqlManager.MySQL == null
					|| sqlManager.c == null){			
				sql = false;
			}
		}
		//SQL setup...
	}

}
