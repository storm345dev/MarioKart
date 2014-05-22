package net.stormdev.mario.mariokart;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import net.stormdev.mario.utils.Colors;

public class MKLang {
	MarioKart plugin = null;
	private static YamlConfiguration lang = new YamlConfiguration();

	public MKLang(MarioKart main) {
		this.plugin = main;
		load();
	}

	public String get(String key) {
		String val = getRaw(key);
		val = Colors.colorise(val);
		return val;
	}
	
	public static String getStr(String key) {
		String val = getRawStr(key);
		val = Colors.colorise(val);
		return val;
	}
	
	public static String getRawStr(String key) {
		if (!lang.contains(key)) {
			return key;
		}
		return lang.getString(key);
	}

	public String getRaw(String key) {
		if (!lang.contains(key)) {
			return key;
		}
		return lang.getString(key);
	}
	
	private void load(){
		File langFile = new File(plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "lang.yml");
		if (langFile.exists() == false || langFile.length() < 1) {
			try {
				langFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}

		}
		try {
			lang.load(langFile);
		} catch (Exception e1) {
			plugin.getLogger().log(Level.WARNING,
					"Error creating/loading lang file! Regenerating..");
		}
		// Setup the Lang file
		if (!lang.contains("error.memoryLockdown")) {
			lang.set("error.memoryLockdown",
					"Operation failed due to lack of System Memory!");
		}
		if (!lang.contains("general.disabled")) {
			lang.set("general.disabled",
					"Error: Disabled");
		}
		if (!lang.contains("general.cmd.leave.success")) {
			lang.set("general.cmd.leave.success",
					"Successfully left %name%!");
		}
		if (!lang.contains("general.cmd.page")) {
			lang.set("general.cmd.page", "Page [%page%/%total%]:");
		}
		if (!lang.contains("general.cmd.full")) {
			lang.set("general.cmd.full",
					"There are no race tracks available!");
		}
		if (!lang.contains("general.cmd.noRaces")) {
			lang.set("general.cmd.noRaces",
					"There are no races for that track current running!");
		}
		if (!lang.contains("general.cmd.forceEnd")) {
			lang.set("general.cmd.forceEnd",
					"Game terminated by an admin!");
		}
		if (!lang.contains("general.cmd.endSuccess")) {
			lang.set("general.cmd.endSuccess",
					"Terminated race!");
		}
		if (!lang.contains("general.cmd.overflow")) {
			lang.set("general.cmd.overflow",
					"Queues/Tracks are full, joining new low-priority queue!");
		}
		if (!lang.contains("general.cmd.playersOnly")) {
			lang.set("general.cmd.playersOnly",
					"This command is for players only!");
		}
		if (!lang.contains("general.cmd.leave.fail")) {
			lang.set("general.cmd.leave.fail", "You aren't in a game/que!");
		}
		if (!lang.contains("general.cmd.setlaps.success")) {
			lang.set("general.cmd.setlaps.success",
					"Successfully set laps for track %name%!");
		}
		if (!lang.contains("general.cmd.setMinPlayers.success")) {
			lang.set("general.cmd.setMinPlayers.success",
					"Successfully set the minimum players for track %name%!");
		}
		if (!lang.contains("general.cmd.setRewards.success")) {
			lang.set("general.cmd.setRewards.success",
					"Successfully set the rewards for track %name%!");
		}
		if (!lang.contains("general.cmd.delete.success")) {
			lang.set("general.cmd.delete.success",
					"Successfully deleted track %name%!");
		}
		if (!lang.contains("general.cmd.delete.exists")) {
			lang.set("general.cmd.delete.exists",
					"That track doesn't exist!");
		}
		if (!lang.contains("general.cmd.racetimes")) {
			lang.set("general.cmd.racetimes",
					"Top %n% times for track %track%:");
		}
		if (!lang.contains("general.shop.notEnoughMoney")) {
			lang.set("general.shop.notEnoughMoney",
					"You don't have enough %currency% for that item!");
		}
		if (!lang.contains("general.shop.maxUpgrades")) {
			lang.set("general.shop.maxUpgrades",
					"You are not allowed to own more than 64 of an upgrade!");
		}
		if (!lang.contains("general.shop.success")) {
			lang.set(
					"general.shop.success",
					"Successfully bought %name% for %price% %currency%! You now have %balance% %currency%!");
		}
		if (!lang.contains("general.shop.sellSuccess")) {
			lang.set("general.shop.sellSuccess",
					"Successfully removed %amount% of %name% from your upgrades list!");
		}
		if (!lang.contains("general.shop.error")) {
			lang.set("general.shop.error",
					"An error occured. Please contact a member of staff. (No economy found)");
		}
		if (!lang.contains("setup.create.exists")) {
			lang.set("setup.create.exists",
					"This track already exists! Please do /urace delete %name% before proceeding!");
		}
		if (!lang.contains("setup.create.start")) {
			lang.set("setup.create.start", "Wand: %id% (%name%)");
		}
		if (!lang.contains("setup.create.lobby")) {
			lang.set("setup.create.lobby",
					"Stand in the lobby and right click anywhere with the wand");
		}
		if (!lang.contains("setup.create.exit")) {
			lang.set("setup.create.exit",
					"Stand at the track exit and right click anywhere with the wand");
		}
		if (!lang.contains("setup.create.grid")) {
			lang.set(
					"setup.create.grid",
					"Stand where you want a car to start the race and right click anywhere (Without the wand). Repeat for all the starting positions. When done, right click anywhere with the wand");
		}
		if (!lang.contains("setup.create.checkpoints")) {
			lang.set(
					"setup.create.checkpoints",
					"Stand at each checkpoint along the track (Checkpoint 10x10 radius) and right click anywhere (Without the wand). Repeat for all checkpoints. When done, right click anywhere with the wand");
		}
		if (!lang.contains("setup.create.notEnoughCheckpoints")) {
			lang.set("setup.create.notEnoughCheckpoints",
					"You must have at least 3 checkpoints! You only have: %num%");
		}
		if (!lang.contains("setup.create.line1")) {
			lang.set(
					"setup.create.line1",
					"Stand at one end of the start/finish line and right click anywhere with the wand");
		}
		if (!lang.contains("setup.create.line2")) {
			lang.set(
					"setup.create.line2",
					"Stand at the other end of the start/finish line and right click anywhere with the wand");
		}
		if (!lang.contains("setup.create.done")) {
			lang.set("setup.create.done",
					"Successfully created Race Track %name%!");
		}
		if (!lang.contains("setup.fail.queueSign")) {
			lang.set("setup.fail.queueSign",
					"That track doesn't exist!");
		}
		if (!lang.contains("setup.create.queueSign")) {
			lang.set("setup.create.queueSign",
					"Successfully registered queue sign!");
		}
		if (!lang.contains("race.que.existing")) {
			lang.set("race.que.existing",
					"You are already in a game/que! Please leave it before joining this one!");
		}
		if (!lang.contains("race.que.other")) {
			lang.set("race.que.other",
					"Unavailable! Current queue race type: %type%");
		}
		if (!lang.contains("race.que.full")) {
			lang.set("race.que.full", "Race que full!");
		}
		if (!lang.contains("race.que.success")) {
			lang.set("race.que.success", "In Race Que!");
		}
		if (!lang.contains("race.que.joined")) {
			lang.set("race.que.joined", " joined the race que!");
		}
		if (!lang.contains("race.que.left")) {
			lang.set("race.que.left", " left the race que!");
		}
		if (!lang.contains("race.que.players")) {
			lang.set(
					"race.que.players",
					"Acquired minimum players for race! Waiting %time% seconds for additional players to join...");
		}
		if (!lang.contains("race.que.preparing")) {
			lang.set("race.que.preparing", "Preparing race...");
		}
		if (!lang.contains("race.que.starting")) {
			lang.set("race.que.starting", "Race starting in...");
		}
		if (!lang.contains("resource.download")) {
			lang.set("resource.download", "Downloading resources...");
		}
		if (!lang.contains("resource.downloadHelp")) {
			lang.set("resource.downloadHelp",
					"If the resources aren't downloaded automatically. Download it at: %url%");
		}
		if (!lang.contains("resource.clear")) {
			lang.set("resource.clear",
					"Switching back to default minecraft textures...");
		}
		if (!lang.contains("race.que.go")) {
			lang.set("race.que.go", "Go!");
		}
		if (!lang.contains("race.end.timeLimit")) {
			lang.set("race.end.timeLimit", "Time Limit exceeded!");
		}
		if (!lang.contains("race.end.won")) {
			lang.set("race.end.won", " won the race!");
		}
		if (!lang.contains("race.end.rewards")) {
			lang.set("race.end.rewards",
					"&6+&a%amount%&6 %currency% for %position%! You now have %balance% %currency%!");
		}
		if (!lang.contains("race.end.time")) {
			lang.set("race.end.time", "Your time was %time% seconds!");
		}
		if (!lang.contains("race.mid.miss")) {
			lang.set("race.mid.miss",
					"You missed a section of the track! Please go back and do it!");
		}
		if (!lang.contains("race.mid.backwards")) {
			lang.set("race.mid.backwards", "You are going the wrong way!");
		}
		if (!lang.contains("race.mid.lap")) {
			lang.set("race.mid.lap", "Lap [%lap%/%total%]");
		}
		if (!lang.contains("race.end.soon")) {
			lang.set("race.end.soon",
					"You have 1 minute before the race ends!");
		}
		if (!lang.contains("race.end.position")) {
			lang.set("race.end.position", "You finished %position%!");
		}
		if (!lang.contains("race.upgrades.use")) {
			lang.set("race.upgrades.use", "&c[-]&6 Consumed Upgrade");
		}
		if (!lang.contains("mario.hit")) {
			lang.set("mario.hit", "You were hit by a %name%!");
		}
		if(!lang.contains("mario.shop.title")){
			lang.set("mario.shop.title", "MarioKart Shop");
		}
		if(!lang.contains("mario.shop.exit.title")){
			lang.set("mario.shop.exit.title", "Exit Menu");
		}
		if(!lang.contains("mario.shop.exit.info")){
			lang.set("mario.shop.exit.info", "Exit this Menu!");
		}
		if(!lang.contains("mario.shop.buyUpgrades.title")){
			lang.set("mario.shop.buyUpgrades.title", "Buy Upgrades");
		}
		if(!lang.contains("mario.shop.buyUpgrades.info")){
			lang.set("mario.shop.buyUpgrades.info", "Upgrade your Kart!");
		}
		if(!lang.contains("mario.shop.myUpgrades.title")){
			lang.set("mario.shop.myUpgrades.title", "My Upgrades");
		}
		if(!lang.contains("mario.shop.myUpgrades.info")){
			lang.set("mario.shop.myUpgrades.info", "View and Remove Kart upgrades!");
		}
		if(!lang.contains("mario.shop.page")){
			lang.set("mario.shop.page", "Page:");
		}
		if(!lang.contains("mario.shop.back.title")){
			lang.set("mario.shop.back.title", "Back to menu");
		}
		if(!lang.contains("mario.shop.back.info")){
			lang.set("mario.shop.back.info", "Return back to the selection menu");
		}
		if(!lang.contains("mario.shop.back.previous.title")){
			lang.set("mario.shop.back.previous.title", "Previous Page");
		}
		if(!lang.contains("mario.shop.back.previous.info")){
			lang.set("mario.shop.back.previous.info", "Go to the previous page");
		}
		if(!lang.contains("mario.shop.back.next.title")){
			lang.set("mario.shop.back.next.title", "Next Page");
		}
		if(!lang.contains("mario.shop.next.info")){
			lang.set("mario.shop.next.info", "Go to the next page");
		}
		if(!lang.contains("mario.shop.price")){
			lang.set("mario.shop.price", "Price: %amount% %currency%");
		}
		if(!lang.contains("mario.shop.delete")){
			lang.set("mario.shop.delete", "Click to delete");
		}
		
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			plugin.getLogger().info("Error saving lang file!");
		} catch (Exception e2){
			plugin.getLogger().info("Error parsing lang file!");
		}
	}
}
