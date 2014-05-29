package net.stormdev.mario.config;

import net.stormdev.mario.utils.LocationStrings;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfigurator {
	private static double current = 1.1;
	
	public static void load(FileConfiguration config){
		fill(config);
	}
	private static void fill(FileConfiguration config){
		System.out.println("Reading config...");
		// Setup the config
		if (!config.contains("setup.create.wand")) {
			config.set("setup.create.wand", 280);
		}
		else{
			//Config has been generated before
			double version;
			if(!config.contains("misc.configVersion")){
				version = 1;
			}
			else{
				version = config.getDouble("misc.configVersion");
			}
			if(version < current){
				System.out.println("Converting config...");
				config = ConfigVersionConverter.convert(config, current);
			}
		}
		if(!config.contains("misc.configVersion")){
			config.set("misc.configVersion", current);
		}
		if(!config.contains("general.server.control")){
			config.set("general.server.control", false);
		}
		if(!config.contains("general.server.bungeelobby")){
			config.set("general.server.bungeelobby", "lobby");
		}
		if(!config.contains("general.server.gamelobby")){
			config.set("general.server.gamelobby", LocationStrings.getLocationString(Bukkit.getWorlds().get(0).getSpawnLocation()));
		}
		if (!config.contains("general.logger.colour")) {
			config.set("general.logger.colour", true);
		}
		if (!config.contains("general.raceLimit")) {
			config.set("general.raceLimit", 10);
		}
		if (!config.contains("general.raceTickrate")) {
			config.set("general.raceTickrate", 4l);
		}
		if (!config.contains("general.checkpointRadius")) {
			config.set("general.checkpointRadius", 10.0);
		}
		if (!config.contains("general.raceGracePeriod")) {
			config.set("general.raceGracePeriod", 10.0);
		}
		if (!config.contains("general.race.timed.log")) {
			config.set("general.race.timed.log", true);
		}
		if (!config.contains("general.race.maxTimePerCheckpoint")) {
			config.set("general.race.maxTimePerCheckpoint", 60);
		}
		if (!config.contains("general.race.enableTimeLimit")) {
			config.set("general.race.enableTimeLimit", true);
		}
		if (!config.contains("general.race.targetPlayers")) {
			config.set("general.race.targetPlayers", 5);
		}
		if (!config.contains("general.race.rewards.enable")) {
			config.set("general.race.rewards.enable", true);
		}
		if (!config.contains("general.race.rewards.win")) {
			config.set("general.race.rewards.win", 10.0);
		}
		if (!config.contains("general.race.rewards.second")) {
			config.set("general.race.rewards.second", 5.0);
		}
		if (!config.contains("general.race.rewards.third")) {
			config.set("general.race.rewards.third", 2.0);
		}
		if (!config.contains("general.race.rewards.currency")) {
			config.set("general.race.rewards.currency", "Dollars");
		}
		if (!config.contains("general.race.music.enable")) {
			config.set("general.race.music.enable", true);
		}
		if(!config.contains("general.race.waterDamage.enable")){
			config.set("general.race.waterDamage.enable", true);
		}
		if (!config.contains("general.race.waterDamage.tracks")) {
			config.set("general.race.waterDamage.tracks", new String[]{"TrackName", "TrackName"});
		}
		if(!config.contains("general.race.lavaDamage.enable")){
			config.set("general.race.lavaDamage.enable", true);
		}
		if (!config.contains("general.race.lavaDamage.tracks")) {
			config.set("general.race.lavaDamage.tracks", new String[]{"TrackName", "TrackName"});
		}
		if(!config.contains("general.upgrades.enable")){
			config.set("general.upgrades.enable", true);
		}
		if (!config.contains("general.upgrades.useSQL")) {
			config.set("general.upgrades.useSQL", false);
		}
		if (!config.contains("general.upgrades.sqlHostName")) {
			config.set("general.upgrades.sqlHostName", "localhost");
		}
		if (!config.contains("general.upgrades.sqlPort")) {
			config.set("general.upgrades.sqlPort", "3306");
		}
		if (!config.contains("general.upgrades.sqlDataBaseName")) {
			config.set("general.upgrades.sqlDataBaseName", "myDataBase");
		}
		if (!config.contains("general.upgrades.sqlUsername")) {
			config.set("general.upgrades.sqlUsername", "root");
		}
		if (!config.contains("general.upgrades.sqlPassword")) {
			config.set("general.upgrades.sqlPassword", "password123");
		}
		if (!config.contains("general.ensureEqualCarSpeed")) {
			config.set("general.ensureEqualCarSpeed", true);
		}
		if (!config.contains("race.que.minPlayers")) {
			config.set("race.que.minPlayers", 2);
		}
		if (!config.contains("general.optimiseAtRuntime")) {
			config.set("general.optimiseAtRuntime", true);
		}
		if (!config.contains("bitlyUrlShortner")) {
			config.set("bitlyUrlShortner", true);
		}
		if (!config.contains("mariokart.resourcePack") || 
				config.getString("mariokart.resourcePack").equalsIgnoreCase( //Stop dropbox link due to bandwidth over-use
						"https://dl.dropboxusercontent.com/u/147363358/MarioKart/Resource/MarioKart-latest.zip")
				||
				config.getString("mariokart.resourcePack").equalsIgnoreCase( //Stop dropbox link due to bandwidth over-use
						"http://storm345.hj.cx/Uploads/Bukkit/MarioKart/MarioKart-latest.zip")
				) {
			//https://dl.dropboxusercontent.com/u/147363358/MarioKart/Resource/MarioKart-latest.zip
			//http://storm345.hj.cx/Uploads/Bukkit/MarioKart/MarioKart-latest.zip
			config.set(
					"mariokart.resourcePack",
					"DEFAULT_CURSEFORGE_PACK");
		}
		if (!config.contains("mariokart.enable")) {
			config.set("mariokart.enable", true);
		}
		if (!config.contains("mariokart.redShell")) {
			config.set("mariokart.redShell", "INK_SACK:1");
		}
		if (!config.contains("mariokart.greenShell")) {
			config.set("mariokart.greenShell", "INK_SACK:2");
		}
		if (!config.contains("mariokart.blueShell")) {
			config.set("mariokart.blueShell", "INK_SACK:12");
		}
		if (!config.contains("mariokart.banana")) {
			config.set("mariokart.banana", "INK_SACK:11");
		}
		if (!config.contains("mariokart.star")) {
			config.set("mariokart.star", "NETHER_STAR:0");
		}
		if (!config.contains("mariokart.lightning")) {
			config.set("mariokart.lightning", "INK_SACK:7");
		}
		if (!config.contains("mariokart.bomb")) {
			config.set("mariokart.bomb", "TNT:0");
		}
		if (!config.contains("mariokart.boo")) {
			config.set("mariokart.boo", "BONE");
		}
		if (!config.contains("mariokart.pow")) {
			config.set("mariokart.pow", "ICE");
		}
		if (!config.contains("mariokart.random")) {
			config.set("mariokart.random", "STAINED_CLAY:4");
		}
		if (!config.contains("mariokart.mushroom")) {
			config.set("mariokart.mushroom", "RED_MUSHROOM");
		}
		// Setup the colour scheme
		if (!config.contains("colorScheme.success")) {
			config.set("colorScheme.success", "&c");
		}
		if (!config.contains("colorScheme.error")) {
			config.set("colorScheme.error", "&7");
		}
		if (!config.contains("colorScheme.info")) {
			config.set("colorScheme.info", "&6");
		}
		if (!config.contains("colorScheme.title")) {
			config.set("colorScheme.title", "&4");
		}
		if (!config.contains("colorScheme.tp")) {
			config.set("colorScheme.tp", "&1");
		}
	}
}
