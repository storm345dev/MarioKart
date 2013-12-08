package net.stormdev.mario.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UnlockableManager {
	
	private Map<String, Map<String, Unlockable>> data = new HashMap<String, Map<String, Unlockable>>();
	private File saveFile = null;
	private Boolean sql = false;
	public UnlockableManager(File saveFile, Boolean sql){
		this.saveFile = saveFile;
		this.sql = false;
	}

}
