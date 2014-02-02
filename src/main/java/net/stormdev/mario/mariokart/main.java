package net.stormdev.mario.mariokart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.stormdev.mario.config.PluginConfigurator;
import net.stormdev.mario.hotbar.HotBarManager;
import net.stormdev.mario.hotbar.HotBarUpgrade;
import net.stormdev.mario.lesslag.DynamicLagReducer;
import net.stormdev.mario.powerups.PowerupManager;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.queues.RaceQueueManager;
import net.stormdev.mario.queues.RaceScheduler;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceMethods;
import net.stormdev.mario.shop.Shop;
import net.stormdev.mario.shop.Unlockable;
import net.stormdev.mario.shop.UnlockableManager;
import net.stormdev.mario.signUtils.SignManager;
import net.stormdev.mario.sound.MarioKartSound;
import net.stormdev.mario.sound.MusicManager;
import net.stormdev.mario.tracks.RaceTimes;
import net.stormdev.mario.tracks.RaceTrackManager;
import net.stormdev.mario.tracks.TrackCreator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Url;
import com.useful.uCarsAPI.uCarsAPI;
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
	public RaceScheduler raceScheduler = null;
	public static HashMap<String, TrackCreator> trackCreators = new HashMap<String, TrackCreator>();
	public ConcurrentHashMap<String, LinkedHashMap<UUID, RaceQueue>> queues = new ConcurrentHashMap<String, LinkedHashMap<UUID, RaceQueue>>();
	public RaceQueueManager raceQueues = null;
	public static Lang msgs = null;
	public RaceMethods raceMethods = null;
	public Random random = null;
	public static PowerupManager marioKart = null;
	public RaceTimes raceTimes = null;
	public String packUrl = "";
	public HotBarManager hotBarManager = null;
	public double checkpointRadiusSquared = 10.0;
	public List<String> resourcedPlayers = new ArrayList<String>();
	
	public MusicManager musicManager = null;
	
	public static boolean dynamicLagReduce = true;

	Map<String, Unlockable> unlocks = null;

	public UnlockableManager upgradeManager = null;
	public SignManager signManager = null;

	public BukkitTask lagReducer = null;

	public static Boolean vault = false;
	public static Economy economy = null;

	@Override
	public void onEnable() {
		System.gc();
		if (listener != null || cmdExecutor != null || logger != null
				|| msgs != null || marioKart != null || economy != null) {
			getLogger().log(Level.WARNING,
					"Previous plugin instance found, performing clearup...");
			listener = null;
			cmdExecutor = null;
			logger = null;
			msgs = null;
			marioKart = null;
			vault = null;
			economy = null;
		}
		random = new Random();
		plugin = this;
		File queueSignFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "Data" + File.separator + "queueSigns.signData");
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
			// Setup the config
			PluginConfigurator.load(config); //Loads and converts configs
		} catch (Exception e) {
		}
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		uCarsAPI.getAPI().hookPlugin(this);
		// Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		logger.info("Config loaded!");
		this.checkpointRadiusSquared = Math.pow(
				config.getDouble("general.checkpointRadius"), 2);
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
		logger.info("Searching for ProtocolLib...");
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
		this.musicManager = new MusicManager(this);
		main.listener = new URaceListener(this);
		getServer().getPluginManager().registerEvents(main.listener, this);
		this.trackManager = new RaceTrackManager(this, new File(getDataFolder()
				+ File.separator + "Data" + File.separator
				+ "tracks.uracetracks"));
		this.raceQueues = new RaceQueueManager();
		this.raceMethods = new RaceMethods();
		this.raceScheduler = new RaceScheduler(
				config.getInt("general.raceLimit"));
		// Setup marioKart
		marioKart = new PowerupManager(this);
		this.raceTimes = new RaceTimes(new File(getDataFolder()
				+ File.separator + "Data" + File.separator
				+ "raceTimes.uracetimes"),
				config.getBoolean("general.race.timed.log"));
		if (config.getBoolean("general.race.rewards.enable")) {
			try {
				vault = this.vaultInstalled();
				if (!setupEconomy()) {
					plugin.getLogger()
							.warning(
									"Attempted to enable rewards but Vault/Economy NOT found. Please install vault to use this feature!");
					plugin.getLogger().warning("Disabling reward system...");
					config.set("general.race.rewards.enable", false);
				}
			} catch (Exception e) {
				plugin.getLogger()
						.warning(
								"Attempted to enable rewards and shop but Vault/Economy NOT found. Please install vault to use these features!");
				plugin.getLogger().warning("Disabling reward system...");
				plugin.getLogger().warning("Disabling shop system...");
				main.config.set("general.race.rewards.enable", false);
				main.config.set("general.upgrades.enable", false);
			}
		}
		String rl = main.config.getString("mariokart.resourcePack");

		Boolean valid = true;
		try {
			new URL(rl);
		} catch (MalformedURLException e2) {
			valid = false;
		}
		if (valid && main.config.getBoolean("bitlyUrlShortner")) {
			// Shorten url
			// Generic access token: 3676e306c866a24e3586a109b9ddf36f3d177556
			Url url = Bitly
					.as("storm345", "R_b0fae26d68750227470cd06b23be70b7").call(
							Bitly.shorten(rl));
			this.packUrl = url.getShortUrl();
		} else {
			this.packUrl = rl;
		}
		this.upgradeManager = new UnlockableManager(new File(getDataFolder()
				.getAbsolutePath()
				+ File.separator
				+ "Data"
				+ File.separator
				+ "upgradesData.mkdata"),
				config.getBoolean("general.upgrades.useSQL"), getUnlocks());
		this.hotBarManager = new HotBarManager(config.getBoolean("general.upgrades.enable"));
		this.lagReducer = getServer().getScheduler().runTaskTimer(this,
				new DynamicLagReducer(), 100L, 1L);
		
		this.signManager = new SignManager(queueSignFile);
		dynamicLagReduce = config.getBoolean("general.optimiseAtRuntime");
		
		if(!dynamicLagReduce){
			logger.info(ChatColor.RED+"[WARNING] The plugin's self optimisation has been disabled,"
					+ " this is risky as if one config value isn't set optimally - MarioKart has a chance"
					+ " of crashing your server! I recommend you turn it back on!");
			try {
				Thread.sleep(1000); //Show it to then for 1s
			} catch (InterruptedException e) {}
		}
		
		System.gc();
		logger.info("MarioKart v" + plugin.getDescription().getVersion()
				+ " has been enabled!");
	}

	@Override
	public void onDisable() {
		if (ucars != null) {
			ucars.unHookPlugin(this);
		}
		HashMap<UUID, Race> races = new HashMap<UUID, Race>(
				this.raceScheduler.getRaces());
		for (UUID id : races.keySet()) {
			races.get(id).end(); // End the race
		}
		raceQueues.clear();
		Player[] players = getServer().getOnlinePlayers().clone();
		for (Player player : players) {
			if (player.hasMetadata("car.stayIn")) {
				player.removeMetadata("car.stayIn", plugin);
			}
		}
		this.lagReducer.cancel();
		getServer().getScheduler().cancelTasks(this);
		System.gc();
		try {
			Shop.getShop().destroy();
		} catch (Exception e) {
			// Shop is invalid anyway
		}
		this.upgradeManager.unloadSQL();
		logger.info("MarioKart has been disabled!");
		System.gc();
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

	public boolean vaultInstalled(){
		Plugin[] plugins = getServer().getPluginManager().getPlugins();
		for (Plugin p : plugins) {
			if (p.getName().equals("Vault")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean setupEconomy() {
		if(!vault){
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public Map<String, Unlockable> getUnlocks() {
		if (unlocks != null) {
			return unlocks;
		}
		main.logger.info("Loading upgrades...");
		// Begin load them from a YAML file
		Map<String, Unlockable> unlockables = new HashMap<String, Unlockable>();
		File saveFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "upgrades.yml");
		YamlConfiguration upgrades = new YamlConfiguration();
		saveFile.getParentFile().mkdirs();
		Boolean setDefaults = false;
		try {
			upgrades.load(saveFile);
		} catch (Exception e) {
			setDefaults = true;
		}
		if (!saveFile.exists() || saveFile.length() < 1 || setDefaults) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				return unlockables;
			}
			// Set defaults
			upgrades.set("upgrades.speedBurstI.name", "Speed Burst I (5s)");
			upgrades.set("upgrades.speedBurstI.id", "aa");
			upgrades.set("upgrades.speedBurstI.type", HotBarUpgrade.SPEED_BOOST
					.name().toUpperCase());
			upgrades.set("upgrades.speedBurstI.item", Material.APPLE.name()
					.toUpperCase());
			upgrades.set("upgrades.speedBurstI.length", 5000l);
			upgrades.set("upgrades.speedBurstI.power", 10d);
			upgrades.set("upgrades.speedBurstI.useItem", true);
			upgrades.set("upgrades.speedBurstI.useUpgrade", true);
			upgrades.set("upgrades.speedBurstI.price", 3d);
			upgrades.set("upgrades.speedBurstII.name", "Speed Burst II (10s)");
			upgrades.set("upgrades.speedBurstII.id", "ab");
			upgrades.set("upgrades.speedBurstII.type",
					HotBarUpgrade.SPEED_BOOST.name().toUpperCase());
			upgrades.set("upgrades.speedBurstII.item", Material.CARROT_ITEM
					.name().toUpperCase());
			upgrades.set("upgrades.speedBurstII.length", 10000l);
			upgrades.set("upgrades.speedBurstII.power", 13d);
			upgrades.set("upgrades.speedBurstII.useItem", true);
			upgrades.set("upgrades.speedBurstII.useUpgrade", true);
			upgrades.set("upgrades.speedBurstII.price", 6d);
			upgrades.set("upgrades.immunityI.name", "Immunity I (5s)");
			upgrades.set("upgrades.immunityI.id", "ac");
			upgrades.set("upgrades.immunityI.type", HotBarUpgrade.IMMUNITY
					.name().toUpperCase());
			upgrades.set("upgrades.immunityI.item", Material.IRON_HELMET.name()
					.toUpperCase());
			upgrades.set("upgrades.immunityI.length", 5000l);
			upgrades.set("upgrades.immunityI.useItem", true);
			upgrades.set("upgrades.immunityI.useUpgrade", true);
			upgrades.set("upgrades.immunityI.price", 6d);
			upgrades.set("upgrades.immunityII.name", "Immunity II (10s)");
			upgrades.set("upgrades.immunityII.id", "ad");
			upgrades.set("upgrades.immunityII.type", HotBarUpgrade.IMMUNITY
					.name().toUpperCase());
			upgrades.set("upgrades.immunityII.item", Material.GOLD_HELMET
					.name().toUpperCase());
			upgrades.set("upgrades.immunityII.length", 10000l);
			upgrades.set("upgrades.immunityII.useItem", true);
			upgrades.set("upgrades.immunityII.useUpgrade", true);
			upgrades.set("upgrades.immunityII.price", 12d);
			try {
				upgrades.save(saveFile);
			} catch (IOException e) {
				main.logger.info(main.colors.getError()
						+ "[WARNING] Failed to create upgrades.yml!");
			}
		}
		// Load them
		ConfigurationSection ups = upgrades.getConfigurationSection("upgrades");
		Set<String> upgradeKeys = ups.getKeys(false);
		for (String key : upgradeKeys) {
			ConfigurationSection sect = ups.getConfigurationSection(key);
			if (!sect.contains("name") || !sect.contains("type")
					|| !sect.contains("id") || !sect.contains("useItem")
					|| !sect.contains("useUpgrade") || !sect.contains("price")
					|| !sect.contains("item")) {
				// Invalid upgrade
				main.logger.info(main.colors.getError()
						+ "[WARNING] Invalid upgrade: " + key);
				continue;
			}
			String name = sect.getString("name");
			HotBarUpgrade type = null;
			Material item = null;
			try {
				type = HotBarUpgrade.valueOf(sect.getString("type"));
				item = Material.valueOf(sect.getString("item"));
			} catch (Exception e) {
				// Invalid upgrade
				main.logger.info(main.colors.getError()
						+ "[WARNING] Invalid upgrade: " + key);
				continue;
			}
			if (type == null || item == null) {
				// Invalid upgrade
				main.logger.info(main.colors.getError()
						+ "[WARNING] Invalid upgrade: " + key);
				continue;
			}
			String shortId = sect.getString("id");
			Boolean useItem = sect.getBoolean("useItem");
			Boolean useUpgrade = sect.getBoolean("useUpgrade");
			double price = sect.getDouble("price");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("upgrade.name", name);
			data.put("upgrade.useItem", useItem);
			data.put("upgrade.useUpgrade", useUpgrade);
			if (sect.contains("power")) {
				data.put("upgrade.power", sect.getDouble("power"));
			}
			if (sect.contains("length")) {
				data.put("upgrade.length", sect.getLong("length"));
			}
			Unlockable unlock = new Unlockable(type, data, price, name,
					shortId, item);
			unlockables.put(shortId, unlock);
		}
		unlocks = unlockables;
		return unlockables;
	}
	
	@SuppressWarnings("deprecation")
	public Boolean playCustomSound(final Player recipient, final Location location, 
			final String soundPath, final float volume, final float pitch){
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			@Override
			public void run() {
				//Running async keeps TPS higher
				recipient.playSound(location, soundPath, volume, pitch); //Deprecated but still best way
			}});
		return true;
		/* Not needed
		if(main.prototcolManager == null){
			//No protocolLib
			return false;
		}
		getServer().getScheduler().runTaskAsynchronously(this, new BukkitRunnable(){
			@Override
			public void run() {
				//Play the sound
				try {
					if(pitch > 255){
						pitch = 255;
					}
					PacketContainer customSound = main.prototcolManager.createPacket(PacketType.Play.Server.NAMED_SOUND_EFFECT);
					customSound.getSpecificModifier(String.class).
					    write(0, soundPath);
					customSound.getSpecificModifier(int.class).
					    write(0, location.getBlockX()).
					    write(1, location.getBlockY()).
					    write(2, location.getBlockZ());
					    write(3, (int) pitch);
					customSound.getSpecificModifier(float.class).
					    write(0, volume);
					main.prototcolManager.sendServerPacket(recipient, customSound);
				} catch (Exception e) {
					main.logger.info(main.colors.getError()+"Error playing custom sound: "+soundPath+"!");
					e.printStackTrace();
					return;
				}
				return;
			}});
		return true;
		*/
	}
	
	public Boolean playCustomSound(Player recipient, Location location,
			MarioKartSound sound, float volume, float pitch){
		return playCustomSound(recipient, location, sound.getPath(), volume, pitch);
	}
	
	public Boolean playCustomSound(Player recipient, MarioKartSound sound){
		return playCustomSound(recipient, recipient.getLocation(),
				sound, Float.MAX_VALUE, 1f);
	}
}
