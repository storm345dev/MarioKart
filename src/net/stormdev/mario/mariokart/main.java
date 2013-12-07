package net.stormdev.mario.mariokart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.stormdev.mario.utils.RaceMethods;
import net.stormdev.mario.utils.RaceQueue;
import net.stormdev.mario.utils.RaceQueueManager;
import net.stormdev.mario.utils.RaceTrackManager;
import net.stormdev.mario.utils.TrackCreator;
import net.stormdev.mariokartAddons.MarioKart;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Url;
import com.useful.ucars.Colors;
import com.useful.ucars.ucars;

public class main extends JavaPlugin {
	public YamlConfiguration lang = new YamlConfiguration();
	public static main plugin;
	public static FileConfiguration config = new YamlConfiguration();
	public static Colors colors;
	public static CustomLogger logger = null;
	public static ucars ucars = null;
	public static URaceCommandExecutor cmdExecutor = null;
	public static URaceListener listener = null;
	public RaceTrackManager trackManager = null;
	public OldRaceScheduler gameScheduler = null;
	public static HashMap<String, TrackCreator> trackCreators = new HashMap<String, TrackCreator>();
	public HashMap<String, Map<UUID,RaceQueue>> queues = new HashMap<String, Map<UUID,RaceQueue>>();
	public RaceQueueManager raceQueues = null;
	public static Lang msgs = null;
	public RaceMethods raceMethods = null;
	public Random random = null;
	public static MarioKart marioKart = null;
	public RaceTimes raceTimes = null;
	public String packUrl = "";
	
	public static Boolean vault = false;
	public static Economy economy = null;

	public void onEnable() {
		random = new Random();
		plugin = this;
		File langFile = new File(getDataFolder().getAbsolutePath()
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
			getLogger().log(Level.WARNING,
					"Error creating/loading lang file! Regenerating..");
		}
		msgs = new Lang(this);
		if (new File(getDataFolder().getAbsolutePath() + File.separator
				+ "config.yml").exists() == false
				|| new File(getDataFolder().getAbsolutePath() + File.separator
						+ "config.yml").length() < 1) {
			getDataFolder().mkdirs();
			File configFile = new File(getDataFolder().getAbsolutePath()
					+ File.separator + "config.yml");
			try {
				configFile.createNewFile();
			} catch (IOException e) {
			}
			copy(getResource("marioKartConfigHeader.yml"), configFile);
		}
		config = getConfig();
		logger = new CustomLogger(getServer().getConsoleSender(), getLogger());
		try {
			// Setup the Lang file
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
				lang.set("race.end.rewards", "&6+&a%amount%&6 %currency% for %position%! You now have %balance% %currency%!");
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
			if (!lang.contains("mario.hit")) {
				lang.set("mario.hit", "You were hit by a %name%!");
			}
			// Setup the config
			if (!config.contains("setup.create.wand")) {
				config.set("setup.create.wand", 280);
			}
			if (!config.contains("general.logger.colour")) {
				config.set("general.logger.colour", true);
			}
			if (!config.contains("general.raceLimit")) {
				config.set("general.raceLimit", 10);
			}
			if (!config.contains("general.raceTickrate")) {
				config.set("general.raceTickrate", 2l);
			}
			if (!config.contains("general.raceGracePeriod")) {
				config.set("general.raceGracePeriod", (double) 10.0);
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
			if (!config.contains("race.que.minPlayers")) {
				config.set("race.que.minPlayers", 2);
			}
			if (!config.contains("bitlyUrlShortner")) {
				config.set("bitlyUrlShortner", true);
			}
			if (!config.contains("mariokart.resourcePack")) {
				config.set(
						"mariokart.resourcePack",
						"https://dl.dropboxusercontent.com/u/147363358/MarioKart/Resource/MarioKart-latest.zip");
			}
			if (!config.contains("mariokart.resourceNonMarioPack")) {
				config.set("mariokart.resourceNonMarioPack",
						"https://dl.dropboxusercontent.com/u/147363358/MarioKart/Resource/defaults.zip");
			}
			if (!config.contains("mariokart.enable")) {
				config.set("mariokart.enable", true);
			}
			if (!config.contains("mariokart.redShell")) {
				config.set("mariokart.redShell", "351:1");
			}
			if (!config.contains("mariokart.greenShell")) {
				config.set("mariokart.greenShell", "351:2");
			}
			if (!config.contains("mariokart.blueShell")) {
				config.set("mariokart.blueShell", "351:12");
			}
			if (!config.contains("mariokart.banana")) {
				config.set("mariokart.banana", "351:11");
			}
			if (!config.contains("mariokart.star")) {
				config.set("mariokart.star", "399");
			}
			if (!config.contains("mariokart.lightning")) {
				config.set("mariokart.lightning", "351:7");
			}
			if (!config.contains("mariokart.bomb")) {
				config.set("mariokart.bomb", "46");
			}
			if (!config.contains("mariokart.boo")) {
				config.set("mariokart.boo", "352");
			}
			if (!config.contains("mariokart.pow")) {
				config.set("mariokart.pow", "79");
			}
			if (!config.contains("mariokart.random")) {
				config.set("mariokart.random", "159:4");
			}
			if (!config.contains("mariokart.mushroom")) {
				config.set("mariokart.mushroom", "40");
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
		} catch (Exception e) {
		}
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		// Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		logger.info("Config loaded!");
		logger.info("Searching for uCars...");
		Plugin[] plugins = getServer().getPluginManager().getPlugins();
		Boolean installed = false;
		for (Plugin p : plugins) {
			if (p.getName().equals("uCars")) {
				installed = true;
				ucars = (com.useful.ucars.ucars) p;
			}
		}
		if (!installed) {
			logger.info("Unable to find uCars!");
			getServer().getPluginManager().disablePlugin(this);
		}
		ucars.hookPlugin(this);
		logger.info("uCars found and hooked!");
		PluginDescriptionFile pldesc = plugin.getDescription();
		Map<String, Map<String, Object>> commands = pldesc.getCommands();
		Set<String> keys = commands.keySet();
		main.cmdExecutor = new URaceCommandExecutor(this);
		for (String k : keys) {
			try {
				getCommand(k).setExecutor(cmdExecutor);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE,
						"Error registering command " + k.toString());
				e.printStackTrace();
			}
		}
		main.listener = new URaceListener(this);
		getServer().getPluginManager().registerEvents(main.listener, this);
		this.trackManager = new RaceTrackManager(this, new File(getDataFolder()
				+ File.separator + "Data" + File.separator
				+ "tracks.uracetracks"));
		this.raceQueues = new RaceQueueManager();
		this.raceMethods = new RaceMethods();
		this.gameScheduler = new OldRaceScheduler();
		// Setup marioKart
		marioKart = new MarioKart(this);
		this.raceTimes = new RaceTimes(new File(getDataFolder()
				+ File.separator + "Data" + File.separator
				+ "raceTimes.uracetimes"),
				config.getBoolean("general.race.timed.log"));
		if (config.getBoolean("general.race.rewards.enable")) {
			try {
				if (!setupEconomy()) {
					plugin.getLogger()
							.warning(
									"Attempted to enable rewards but Vault/Economy NOT found. Please install vault to use this feature!");
					plugin.getLogger().warning("Disabling reward system...");
					config.set("general.race.rewards.enable", false);
				} else {
					vault = true;
				}
			} catch (Exception e) {
				plugin.getLogger()
				.warning(
						"Attempted to enable rewards but Vault/Economy NOT found. Please install vault to use this feature!");
		        plugin.getLogger().warning("Disabling reward system...");
		        config.set("general.race.rewards.enable", false);
			}
		}
		String rl = main.config.getString("mariokart.resourcePack");
		
		Boolean valid = true;
		try {
			new URL(rl);
		} catch (MalformedURLException e2) {
			valid = false;
		}
		if(valid && main.config.getBoolean("bitlyUrlShortner")){
			//Shorten url
				//Generic access token: 3676e306c866a24e3586a109b9ddf36f3d177556
				Url url = Bitly.as("storm345", "R_b0fae26d68750227470cd06b23be70b7").call(Bitly.shorten(rl));
                this.packUrl = url.getShortUrl();
		}
		else{
			this.packUrl = rl;
		}
		logger.info("MarioKart v" + plugin.getDescription().getVersion()
				+ " has been enabled!");
	}

	public void onDisable() {
		if (ucars != null) {
			ucars.unHookPlugin(this);
		}
		HashMap<String, Race> games = this.gameScheduler.getGames();
		for (Race r : games.values()) {
			r.end();
			try {
				this.gameScheduler.stopGame(r.getTrack(), r.getGameId());
			} catch (Exception e) {
			}
		}
		getServer().getScheduler().cancelTasks(this);
		logger.info("MarioKart has been disabled!");
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				// System.out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String colorise(String prefix) {
		return ChatColor.translateAlternateColorCodes('&', prefix);
	}
	
	protected boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
}
