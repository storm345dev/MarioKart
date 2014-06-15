package net.stormdev.mario.hotbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.stormdev.SQL.MySQL;
import net.stormdev.SQL.SQLManager;
import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.uuidapi.PlayerIDFinder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.useful.ucarsCommon.StatValue;

public class UnlockableManager implements Listener {

	private Map<String, String> data = new HashMap<String, String>();
	private Map<String, Unlockable> unlocks = null; // ShortId:Unlockable
	private File saveFile = null;
	private boolean sql = false;
	private SQLManager sqlManager = null;
	private boolean enabled = true;
	
	private static final String UUID_META = "mariokart.uuid";
	private static final String SQL_TABLE = "MarioKartRaceShop";
	private static final String SQL_KEY = "playerid";
	private static final String SQL_VAL_KEY = "unlocks";
	

	public UnlockableManager(File saveFile, Boolean sql) {
		this.saveFile = saveFile;
		this.sql = sql;
		this.unlocks = getUnlocks();
		this.enabled = MarioKart.config.getBoolean("general.upgrades.enable");
		if (sql) {
			try {
				String sqlHost = MarioKart.config.getString("general.upgrades.sqlHostName");
				String sqlDB = MarioKart.config
						.getString("general.upgrades.sqlDataBaseName");
				int port = Integer.parseInt(MarioKart.config.getString("general.upgrades.sqlPort"));
				String url = "jdbc:mysql://"
						+ sqlHost + ":" + port + "/" + sqlDB;
				
				sqlManager = new SQLManager(new MySQL(MarioKart.plugin, url, MarioKart.config.getString("general.upgrades.sqlUsername"), MarioKart.config.getString("general.upgrades.sqlPassword")), MarioKart.plugin);
			} catch (Exception e) {
				sql = false;
			}
			if (sql) { // Check that it loaded okay...
				sqlManager.createTable(SQL_TABLE, new String[] {
						SQL_KEY, SQL_VAL_KEY }, new String[] {
						"varchar(255) NOT NULL PRIMARY KEY", "varchar(255)" });
			}
		}
		// SQL setup...
		unlocks = getUnlocks();
		load(); // Load the data
		Bukkit.getPluginManager().registerEvents(this, MarioKart.plugin);
	}
	
	public synchronized void unloadSQL(){
		if(this.sqlManager != null){
			this.sqlManager.closeConnection();
		}
		return;
	}

	public List<Upgrade> getUpgrades(Player player) {
		String uuid = getUUID(player);
		if (!data.containsKey(uuid) || !enabled) {
			return new ArrayList<Upgrade>();
		}
		List<Upgrade> upgrades = new ArrayList<Upgrade>();
		String[] unlocks = this.data.get(uuid).split(Pattern.quote(","));
		for (String unlock : unlocks) {
			String[] upgradeData = unlock.split(Pattern.quote(":"));
			if (upgradeData.length > 1) {
				String shortId = upgradeData[0];
				String amount = upgradeData[1];
				if(!this.unlocks.containsKey(shortId)){
					continue;
				}
				int a = 1;
				try {
					a = Integer.parseInt(amount);
				} catch (NumberFormatException e) {
					a = 0;
				}
				if (a > 0) {
					upgrades.add(new Upgrade(this.unlocks.get(shortId), a));
				}
			}
		}
		return upgrades;
	}

	public Boolean useUpgrade(Player player, Upgrade upgrade) {
		if(!enabled){
			return false;
		}
		String uuid = getUUID(player);
		if(!this.data.containsKey(uuid)){
			return false;
		}
		String[] unlocks = this.data.get(uuid).split(Pattern.quote(","));
		String[] un = unlocks.clone();
		Boolean used = false;
		Boolean remove = false;
		Boolean update = false;
		for (int i = 0; i < un.length; i++) {
			remove = false;
			String unlock = un[i];
			String[] upgradeData = unlock.split(Pattern.quote(":"));
			if (upgradeData.length > 1) {
				String shortId = upgradeData[0];
				String amount = upgradeData[1];
				int a = 1;
				try {
					a = Integer.parseInt(amount);
				} catch (NumberFormatException e) {
					a = 0;
				}
				if (a > 0) {
					if (shortId.equals(upgrade.getUnlockedAble().shortId)) {
						int q = a - upgrade.getQuantity();
						if (q < 1) {
							remove = true;
						} else {
							// Set quantity to q
							unlocks[i] = shortId + ":" + q;
						}
						used = true;
					}
				} else {
					remove = true;
				}
			}
			if (remove) {
				unlocks[i] = " ";
				update = true;
			}
		}
		if (used || update) {
			// Update database
			String s = "";
			for (String u : unlocks) {
				if (u.length() > 1) {
					if (s.length() < 1) {
						s = u;
					} else {
						s = s + "," + u;
					}
				}
			}
			if (s.length() < 2) {
				this.data.remove(uuid);
			} else {
				this.data.put(uuid, s);
			}
			save(player); // Save to file/sql
		}
		return used;
	}

	public Boolean addUpgrade(Player player, Upgrade upgrade) {
		if(!enabled){
			return false;
		}
		String uuid = getUUID(player);
		String[] un = new String[] {};
		String[] unlocks = new String[] {};
		if (this.data.containsKey(uuid)) {
			unlocks = this.data.get(uuid).split(Pattern.quote(","));
			un = unlocks.clone();
		}
		Boolean added = false;
		for (int i = 0; i < un.length; i++) {
			String unlock = un[i];
			String[] upgradeData = unlock.split(Pattern.quote(":"));
			if (upgradeData.length > 1) {
				String shortId = upgradeData[0];
				String amount = upgradeData[1];
				int a = 1;
				try {
					a = Integer.parseInt(amount);
				} catch (NumberFormatException e) {
					a = 0;
				}
				if (shortId.equals(upgrade.getUnlockedAble().shortId)) {
					int q = a + upgrade.getQuantity();
					if (q < 1) {
						added = true;
					} else {
						if (q <= 64) {
							// Set quantity to q
							unlocks[i] = shortId + ":" + q;
							added = true;
						} else {
							return false; // Not allowed more than 64 of an
											// upgrade
						}
					}
				}
			}
		}
		// Update database
		String s = "";
		for (String u : unlocks) {
			if (s.length() < 1) {
				s = "" + u;
			} else {
				s += "," + u;
			}
		}
		if (!added) {
			if (s.length() < 1) {
				s = upgrade.getUnlockedAble().shortId + ":"
						+ upgrade.getQuantity();
			} else {
				s += "," + upgrade.getUnlockedAble().shortId + ":"
						+ upgrade.getQuantity();
			}
		}
		if (s.length() < 255) {
			this.data.put(getUUID(player), s);
			save(player); // Save to file/sql
			return true;
		}
		return false; // They have too many upgrades
	}

	public Boolean hasUpgradeById(Player player, String shortId) {
		List<Upgrade> ups = getUpgrades(player);
		for (Upgrade u : ups) {
			if (u.getUnlockedAble().shortId.equals(shortId)) {
				return true;
			}
		}
		return false;
	}

	public Boolean hasUpgradeByName(Player player, String upgradeName) {
		List<Upgrade> ups = getUpgrades(player);
		for (Upgrade u : ups) {
			if (u.getUnlockedAble().upgradeName.equals(upgradeName)) {
				return true;
			}
		}
		return false;
	}

	public void resetUpgrades(Player player) {
		if(!enabled){
			return;
		}
		this.data.remove(getUUID(player));
		save(player);
		return;
	}

	public Unlockable getUnlockable(String shortId) {
		if (!unlocks.containsKey(shortId)) {
			return null;
		}
		return unlocks.get(shortId);
	}

	public String getShortId(String unlockName) {
		List<String> keys = new ArrayList<String>(unlocks.keySet());
		for (String s : keys) {
			Unlockable u = unlocks.get(s);
			if (u.upgradeName.equals(unlockName)) {
				return s;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		if (!sql) {
			if (!(this.saveFile.length() < 1 || !this.saveFile.exists())) {
				// Load from file
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(this.saveFile));
					Object result = ois.readObject();
					ois.close();
					data = (Map<String, String>) result;
				} catch (Exception e) {
					// File just created
				}
			}
		} /*else {
			// Load from sql
			try {
				data.putAll(sqlManager.getStringsFromTable(SQL_TABLE,
						SQL_KEY, SQL_VAL_KEY));
			} catch (SQLException e) {
				// SQL Error
				e.printStackTrace();
			}
		}
		*/
	}
	
	@EventHandler
	void onJoin(PlayerJoinEvent event){
		final Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				String mojangUUID = PlayerIDFinder.getMojangID(player).getID();
				if(mojangUUID == null || mojangUUID.equals("null")){
					mojangUUID = player.getUniqueId().toString();
				}
				player.setMetadata(UUID_META, new StatValue(mojangUUID, MarioKart.plugin));
				load(mojangUUID);
				return;
			}});
	}
	
	@EventHandler
	void onQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		final String uuid = getUUID(player);
		player.removeMetadata(UUID_META, MarioKart.plugin);
		Bukkit.getScheduler().runTaskAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				unload(uuid);
				return;
			}});
	}
	
	public void unload(String playerId){
		data.remove(playerId);
	}
	
	public void load(final String playerId){
		Bukkit.getScheduler().runTaskAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				try {
					Object o = sqlManager.searchTable(SQL_TABLE, SQL_KEY, playerId, SQL_VAL_KEY);
					if(o == null){
						return;
					}
					String s = o.toString();
					
					data.put(playerId, s);
				} catch (SQLException e) {
					//BUGZ
					e.printStackTrace();
				}
				return;
			}});
	}
	
	private String getUUID(Player player){
		if(player.hasMetadata(UUID_META)){
			return player.getMetadata(UUID_META).get(0).value().toString();
		}
		return "NotLoadedProfile";
	}

	public void save(final Player player) {
		MarioKart.plugin.getServer().getScheduler()
				.runTaskAsynchronously(MarioKart.plugin, new Runnable() {

					@Override
					public void run() {
						if (!sql) {
							saveFile.getParentFile().mkdirs();
							if (!saveFile.exists() || saveFile.length() < 1) {
								try {
									saveFile.createNewFile();
								} catch (IOException e) {
								}
							}
							try {
								ObjectOutputStream oos = new ObjectOutputStream(
										new FileOutputStream(saveFile));
								oos.writeObject(data);
								oos.flush();
								oos.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return;
						}
						// Save to SQL
						String uuid = getUUID(player);
						if (data.containsKey(uuid)) {
							try {
								sqlManager.setInTable(SQL_TABLE,
										SQL_KEY, uuid, SQL_VAL_KEY,
										data.get(uuid));
							} catch (SQLException e) {
								// SQL Error
								e.printStackTrace();
							}
						} else {
							try {
								sqlManager.deleteFromTable(SQL_TABLE, SQL_KEY, uuid);
							} catch (SQLException e) {
								// Player wasn't in database
							}
						}
						return;
					}
				});
	}
	
	public void saveFile(){
		save(null);
	}
	
	public Map<String, Unlockable> getUnlocks() {
		if (unlocks != null) {
			return unlocks;
		}
		MarioKart.logger.info("Loading upgrades...");
		// Begin load them from a YAML file
		Map<String, Unlockable> unlockables = new HashMap<String, Unlockable>();
		File saveFile = new File(MarioKart.plugin.getDataFolder().getAbsolutePath()
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
				MarioKart.logger.info(MarioKart.colors.getError()
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
				MarioKart.logger.info(MarioKart.colors.getError()
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
				MarioKart.logger.info(MarioKart.colors.getError()
						+ "[WARNING] Invalid upgrade: " + key);
				continue;
			}
			if (type == null || item == null) {
				// Invalid upgrade
				MarioKart.logger.info(MarioKart.colors.getError()
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

}
