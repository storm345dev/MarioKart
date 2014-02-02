package net.stormdev.mario.players;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class User {
	private Player player;

	private String playerName = "";

	private int checkpoint;

	private int lapsLeft;

	private ItemStack[] oldInventory;

	private final int oldLevel;

	private final float oldExp;

	private boolean inRace;

	private boolean finished;

	private Location location;
	
	private GameMode oldGameMode;
	
	private boolean respawning = false;

	public User(Player player, int oldLevel, float oldExp) {
		this.player = player;
		this.playerName = player.getName();
		this.checkpoint = 0;
		this.lapsLeft = 3;
		this.oldLevel = oldLevel;
		this.oldExp = oldExp;
		try {
			this.oldGameMode = player.getGameMode();
		} catch (Exception e) {
			//Invalid player/they left
			this.oldGameMode = GameMode.SURVIVAL;
		}
		inRace = false;
		finished = false;
		location = null;
	}
	
	public void setRespawning(Boolean respawning){
		this.respawning = respawning;
	}
	
	public GameMode getOldGameMode(){
		return oldGameMode;
	}
	
	public Boolean isRespawning(){
		return respawning;
	}

	public String getPlayerName() {
		return playerName;
	}

	public Player getPlayer() throws PlayerQuitException {
		if(player == null){
			player = MarioKart.plugin.getServer().getPlayer(getPlayerName());
		}
		try {
			if(isRespawning() && player == null){
				return null;
			}
			else if(isRespawning() && player != null){
				setRespawning(false);
			}
			if (player == null || !player.isOnline()) {
				player = null;
				throw new PlayerQuitException(playerName);
			}
		} catch (Exception e) {
			throw new PlayerQuitException(playerName);
		}
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
		return;
	}

	public OfflinePlayer getOfflinePlayer(Server server) {
		return server.getOfflinePlayer(playerName);
	}

	public void setCheckpoint(int checkpoint) {
		this.checkpoint = checkpoint;
	}

	public int getCheckpoint() {
		return checkpoint;
	}

	public void setLapsLeft(int lapsLeft) {
		this.lapsLeft = lapsLeft;
	}

	public int getLapsLeft() {
		return lapsLeft;
	}

	public void setOldInventory(ItemStack[] contents) {
		this.oldInventory = contents;
	}

	public ItemStack[] getOldInventory() {
		return oldInventory;
	}

	public int getOldLevel() {
		return oldLevel;
	}

	public float getOldExp() {
		return oldExp;
	}

	public void setInRace(boolean inRace) {
		this.inRace = inRace;
	}

	public boolean isInRace() {
		return inRace;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof User)) {
			return false;
		}

		User user = (User) object;

		if (!user.getPlayerName().equals(getPlayerName())) {
			return false;
		}

		return true;
	}

	public void clear() {
		this.player = null;
	}
}
