package net.stormdev.mario.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.utils.IdMaterialConverter;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigVersionConverter {
	public static FileConfiguration convert(FileConfiguration config, double target){
		MarioKart.plugin.getLogger().info("Converting config to new format...");
		double td = target*10; //Target is in format n.n
		int t = (int)td;
		MarioKart.plugin.getLogger().info("Config target: "+target);
		switch(t){
		case 11:{ //1.1 
			fromV1ToV2(config); 
			config.set("misc.configVersion", 1.1); //Save that it has been converted
			break;
			}
		default: MarioKart.plugin.getLogger().info("No destination config version found for: "+target);
		}
		return config;
	}
	public static FileConfiguration fromV1ToV2(FileConfiguration config){
		//Convert needed formats
		convertItemFormat(config, "mariokart.redShell");
		convertItemFormat(config, "mariokart.greenShell");
		convertItemFormat(config, "mariokart.blueShell");
		convertItemFormat(config, "mariokart.banana");
		convertItemFormat(config, "mariokart.star");
		convertItemFormat(config, "mariokart.lightning");
		convertItemFormat(config, "mariokart.bomb");
		convertItemFormat(config, "mariokart.boo");
		convertItemFormat(config, "mariokart.pow");
		convertItemFormat(config, "mariokart.random");
		convertItemFormat(config, "mariokart.mushroom");
		MarioKart.plugin.getLogger().info("Config successfully converted!");
		return config;
	}
	public static FileConfiguration convertItemFormat(FileConfiguration config, String configKey){
		String raw = config.getString(configKey);
		String[] rawIds = raw.split(",");
		List<String> newIds = convertItemsToNewFormat(rawIds);
		config.set(configKey, null); //Remove from config
		config.set(configKey, newIds.get(0)); //Save as a stringList
		System.out.println("Converted: "+raw);
		return config;
	}
	public static List<String> convertItemsToNewFormat(String[] rawIds){
		List<String> newIds = new ArrayList<String>();
		for (String raw : rawIds) {
			try {
				final String[] parts = raw.split(":");
				if (parts.length < 1) {
				} else if (parts.length < 2) {
					final int id = Integer.parseInt(parts[0]);
					Material mat = IdMaterialConverter.getMaterialById(id);
					newIds.add(mat.name().toUpperCase());
					continue; //Next iteration
				} else {
					final int id = Integer.parseInt(parts[0]);
					Material mat = IdMaterialConverter.getMaterialById(id);
					final int data = Integer.parseInt(parts[1]);
					String newFormat = mat.name().toUpperCase()+":"+data;
					newIds.add(newFormat);
					continue;
				}
			} catch (Exception e) {
				//Incorrect format also
			}
			MarioKart.plugin.getLogger().info("Invalid config value: "+raw+", skipping...");
		}
	    return newIds;
	}
	public static List<String> convertSpeedModsToNewFormat(String[] rawIds){
		List<String> newIds = new ArrayList<String>();
		for (String raw : rawIds) {
			try {
				String[] segments = raw.split(Pattern.quote("-"));
				final String[] parts = segments[0].split(":");
				String mod = segments[1];
				if (parts.length < 1) {
				} else if (parts.length < 2) {
					final int id = Integer.parseInt(parts[0]);
					Material mat = IdMaterialConverter.getMaterialById(id);
					newIds.add(mat.name().toUpperCase()+"-"+mod);
					continue; //Next iteration
				} else {
					final int id = Integer.parseInt(parts[0]);
					Material mat = IdMaterialConverter.getMaterialById(id);
					final int data = Integer.parseInt(parts[1]);
					String newFormat = mat.name().toUpperCase()+":"+data;
					newIds.add(newFormat+"-"+mod);
					continue;
				}
			} catch (Exception e) {
				//Incorrect format also
			}
			MarioKart.plugin.getLogger().info("Invalid config speedmod: "+raw+", skipping...");
		}
	    return newIds;
	}
}
