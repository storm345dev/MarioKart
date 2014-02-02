package net.stormdev.mario.mariokart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.stormdev.mario.commands.AdminCommandExecutor;
import net.stormdev.mario.commands.RaceCommandExecutor;
import net.stormdev.mario.commands.RaceTimeCommandExecutor;
import net.stormdev.mario.config.PluginConfigurator;
import net.stormdev.mario.events.HotbarEventsListener;
import net.stormdev.mario.events.QueueEventsListener;
import net.stormdev.mario.events.RaceEventsListener;
import net.stormdev.mario.events.ServerEventsListener;
import net.stormdev.mario.events.SignEventsListener;
import net.stormdev.mario.events.TrackEventsListener;
import net.stormdev.mario.hotbar.HotBarManager;
import net.stormdev.mario.lesslag.DynamicLagReducer;
import net.stormdev.mario.powerups.PowerupManager;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.queues.RaceQueueManager;
import net.stormdev.mario.queues.RaceScheduler;
import net.stormdev.mario.races.RaceMethods;
import net.stormdev.mario.shop.Shop;
import net.stormdev.mario.shop.Unlockable;
import net.stormdev.mario.shop.UnlockableManager;
import net.stormdev.mario.signUtils.SignManager;
import net.stormdev.mario.sound.MusicManager;
import net.stormdev.mario.tracks.RaceTimes;
import net.stormdev.mario.tracks.RaceTrackManager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Url;
import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.Colors;
import com.useful.ucars.ucars;

public class MarioKart extends JavaPlugin {
	public static MarioKart plugin;
	public static FileConfiguration config = new YamlConfiguration();
	public static Colors colors;
	public static CustomLogger logger = null;
	public static ucars ucars = null;
	public AdminCommandExecutor adminCommandExecutor = null;
	public RaceCommandExecutor raceCommandExecutor = null;
	public RaceTimeCommandExecutor raceTimeCommandExecutor = null;
	public List<Listener> listeners = null;
	public RaceTrackManager trackManager = null;
	public RaceScheduler raceScheduler = null;
	public ConcurrentHashMap<String, LinkedHashMap<UUID, RaceQueue>> queues = new ConcurrentHashMap<String, LinkedHashMap<UUID, RaceQueue>>();
	public RaceQueueManager raceQueues = null;
	public static MKLang msgs = null;
	public RaceMethods raceMethods = null;
	public Random random = null;
	public static PowerupManager powerupManager = null;
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

	private void setupCmds(){
		adminCommandExecutor = new AdminCommandExecutor(this);
		raceCommandExecutor = new RaceCommandExecutor(this);
		raceTimeCommandExecutor = new RaceTimeCommandExecutor(this);
		
		getCommand("marioRaceAdmin").setExecutor(adminCommandExecutor);
		getCommand("race").setExecutor(raceCommandExecutor);
		getCommand("racetimes").setExecutor(raceTimeCommandExecutor);
		
	}
	
	private void setupListeners(){
		listeners = new ArrayList<Listener>();
		
		listeners.add(new HotbarEventsListener(this));
		listeners.add(new QueueEventsListener(this));
		listeners.add(new RaceEventsListener(this));
		listeners.add(new ServerEventsListener(this));
		listeners.add(new SignEventsListener(this));
		listeners.add(new TrackEventsListener(this));
	}
	
	
	@Override
	public void onEnable() {
		System.gc();
		
		if (listeners != null || logger != null
				|| msgs != null || powerupManager != null || economy != null) {
			getLogger().log(Level.WARNING,
					"Previous plugin instance found, performing clearup...");
			listeners = null;
			logger = null;
			msgs = null;
			powerupManager = null;
			vault = null;
			economy = null;
		}
		
		random = new Random();
		plugin = this;
		
		File queueSignFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "Data" + File.separator + "queueSigns.signData");
		
		msgs = new MKLang(this);
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
		
		// Setup the config
		PluginConfigurator.load(config); //Loads and converts configs
		saveConfig();
		
		uCarsAPI.getAPI().hookPlugin(this);
		ucars = com.useful.ucars.ucars.plugin; //Hook it
		
		// Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		logger.info("Config loaded!");
		this.checkpointRadiusSquared = Math.pow(
				config.getDouble("general.checkpointRadius"), 2);
		
		setupCmds(); //Setup the commands
		setupListeners(); //Setup listeners
 		
		this.musicManager = new MusicManager(this);
		this.trackManager = new RaceTrackManager(this, new File(getDataFolder()
				+ File.separator + "Data" + File.separator
				+ "tracks.uracetracks"));
		this.raceQueues = new RaceQueueManager();
		this.raceMethods = new RaceMethods();
		this.raceScheduler = new RaceScheduler(
				config.getInt("general.raceLimit"));
		
		// Setup marioKart addon to the racing
		powerupManager = new PowerupManager(this);
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
				MarioKart.config.set("general.race.rewards.enable", false);
				MarioKart.config.set("general.upgrades.enable", false);
			}
		}
		String rl = MarioKart.config.getString("mariokart.resourcePack");

		Boolean valid = true;
		try {
			new URL(rl);
		} catch (MalformedURLException e2) {
			valid = false;
		}
		if (valid && MarioKart.config.getBoolean("bitlyUrlShortner")) {
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
				config.getBoolean("general.upgrades.useSQL"));
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
		
		this.raceScheduler.endAll();
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
}
