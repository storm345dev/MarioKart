package net.stormdev.mario.mariokart;

import net.stormdev.mario.utils.PlayerQuitException;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class User {
	private final String player;

	private int checkpoint;

	private int lapsLeft;

	private ItemStack[] oldInventory;

	private final int oldLevel;
	
	private final float oldExp;
	
	private boolean inRace;
	
	private boolean finished;
	
	private Location location;

	public User(String playerName, int oldLevel, float oldExp){
		this.player = playerName;

		this.checkpoint = 0;

		this.lapsLeft = 3;

		this.oldLevel = oldLevel;
		
		this.oldExp = oldExp;
		
		inRace = false;
		
		finished = false;
		
		location = null;
	}

	public String getPlayerName(){
		return player;
	}
	
	public Player getPlayer(Server server) throws PlayerQuitException{
		Player p = server.getPlayer(player);
		if(p==null || !p.isOnline()){
			throw new PlayerQuitException(player);
		}
		return p;
	}
	
	public OfflinePlayer getOfflinePlayer(Server server){
		return server.getOfflinePlayer(player);
	}

	public void setCheckpoint(int checkpoint){
		this.checkpoint = checkpoint;
	}

	public int getCheckpoint(){
		return checkpoint;
	}

	public void setLapsLeft(int lapsLeft){
		this.lapsLeft = lapsLeft;
	}

	public int getLapsLeft(){
		return lapsLeft;
	}
	
	public void setOldInventory(ItemStack[] contents){
		this.oldInventory = contents;
	}

	public ItemStack[] getOldInventory(){
		return oldInventory;
	}

	public int getOldLevel(){
		return oldLevel;
	}
	
	public float getOldExp(){
		return oldExp;
	}
	
	public void setInRace(boolean inRace){
		this.inRace = inRace;
	}
	
	public boolean isInRace(){
		return inRace;
	}
	
	public void setFinished(boolean finished){
		this.finished = finished;
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	public void setLocation(Location location){
		this.location = location;
	}
	
	public Location getLocation(){
		return location;
	}

	@Override
	public boolean equals(Object object){
		if (!(object instanceof User)){
			return false;
		}

		User user = (User) object;

		if (!user.getPlayerName().equals(getPlayerName())){
			return false;
		}

		return true;
	}
}
