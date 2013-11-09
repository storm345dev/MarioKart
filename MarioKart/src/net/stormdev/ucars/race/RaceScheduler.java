package net.stormdev.ucars.race;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import net.stormdev.ucars.utils.RaceQue;
import net.stormdev.ucars.utils.RaceTrack;
import net.stormdev.ucars.utils.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Url;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

/*
 * An adaptation of my Minigamez plugin Arena game scheduler. -Code is messy and 
 * weirdly named as a result
 */
public class RaceScheduler {
	//NOTE: This code is probably highly extraneous in places.
	private HashMap<String, Race> games = new HashMap<String, Race>();
	private main plugin;
	Random random = null;
	public int runningGames = 0;
	public int maxGames = 10;
	public RaceScheduler(){
		this.plugin = main.plugin;
		random = new Random();
		this.maxGames = main.config.getInt("general.raceLimit");
	}
	public Boolean joinGame(String playername, RaceTrack track, RaceQue que, String trackName){
		que.validatePlayers();
		if(que.getHowManyPlayers() < que.getPlayerLimit() && plugin.getServer().getPlayer(playername).isOnline()){
			if(plugin.getServer().getPlayer(playername).isOnline()){
				//que.addPlayer(playername);
				List<String> arenaque = que.getPlayers();
				if(arenaque.contains(playername)){
					plugin.getServer().getPlayer(playername).sendMessage(main.colors.getError()+main.msgs.get("race.que.existing"));
					return true;
				}
				que.addPlayer(playername);
			    //arenaque.add(playername);
				arenaque = que.getPlayers();
				for(String name:arenaque){
					if(!(plugin.getServer().getPlayer(name).isOnline() && plugin.getServer().getPlayer(name) != null)){
						arenaque.remove(name);
						for(String ppname:arenaque){
							if(plugin.getServer().getPlayer(ppname).isOnline() && plugin.getServer().getPlayer(ppname) != null){
								plugin.getServer().getPlayer(ppname).sendMessage(main.colors.getTitle()+"[MarioKart:] "+main.colors.getInfo()+playername+main.msgs.get("race.que.left"));
							}
						}
					}
					else{
						plugin.getServer().getPlayer(name).sendMessage(main.colors.getTitle()+"[MarioKart:] "+main.colors.getInfo()+playername+main.msgs.get("race.que.joined"));
					}
				}
				plugin.raceQues.setQue(trackName, que);
				this.reCalculateQues();
				plugin.getServer().getPlayer(playername).sendMessage(main.colors.getSuccess()+main.msgs.get("race.que.success"));
				plugin.getServer().getPlayer(playername).teleport(track.getLobby(plugin.getServer()));
				String rl = main.config.getString("mariokart.resourcePack");
				Boolean valid = true;
				try {
					new URL(rl);
				} catch (MalformedURLException e2) {
					valid = false;
				}
				if(valid && main.config.getBoolean("bitlyUrlShortner")){
					//Shorten url
					    Player p = plugin.getServer().getPlayer(playername);
						p.sendMessage(main.colors.getInfo()+main.msgs.get("resource.download"));
						//Generic access token: 3676e306c866a24e3586a109b9ddf36f3d177556
						Url url = Bitly.as("storm345", "R_b0fae26d68750227470cd06b23be70b7").call(Bitly.shorten(rl));
						p.sendMessage(main.colors.getInfo()+main.msgs.get("resource.downloadHelp")+ChatColor.RESET+" "+url.getShortUrl());
						p.setTexturePack(rl);
					
				}
				else{
					//Dont shorten url
						Player p=plugin.getServer().getPlayer(playername);
						p.sendMessage(main.colors.getInfo()+main.msgs.get("resource.download"));
						p.sendMessage(main.colors.getInfo()+main.msgs.get("resource.downloadHelp")+ChatColor.RESET+" "+rl);
						p.setTexturePack(rl);
				}
				return true;
			}
		}
		if(plugin.getServer().getPlayer(playername).isOnline()){
			plugin.getServer().getPlayer(playername).sendMessage(main.colors.getError()+main.msgs.get("race.que.full"));
		}
		return false;
	}
	public void reCalculateQues(){
		Set<String> queNames = plugin.raceQues.getQues();
		for(String aname:queNames){
			RaceQue que = plugin.raceQues.getQue(aname);
			List<String> arenaque = que.getPlayers();
			for(String name:arenaque){
				if(!(plugin.getServer().getPlayer(name).isOnline() && plugin.getServer().getPlayer(name) != null)){
					arenaque.remove(name);
				}
			}
			if(que.getTransitioning() == null){
				que.setTransitioning(false);
			}
			if(!trackInUse(aname) && que.getHowManyPlayers() > 1 && !que.getTransitioning() && !(this.runningGames >= this.maxGames)){
				que.setTransitioning(true);
				plugin.raceQues.setQue(aname, que);
				final String queName = aname;
				ArrayList<String> pls = new ArrayList<String>();
				pls.addAll(que.getPlayers());
				double seconds = main.config.getDouble("general.raceGracePeriod");
				double time = seconds*20;
				long grace = (long) time;
				for(String name:pls){
					String msg = main.msgs.get("race.que.players");
					msg = msg.replaceAll(Pattern.quote("%time%"), ""+seconds);
				plugin.getServer().getPlayer(name).sendMessage(main.colors.getInfo()+msg);
				}
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

					public void run() {
						String aname = queName;
						RaceQue arena = main.plugin.raceQues.getQue(aname);
						if(arena.getHowManyPlayers() < 2){
							arena.setTransitioning(false);
							plugin.raceQues.setQue(aname, arena);
							return;
						}
						Race game = new Race(arena.getTrack(), arena.getTrack().getTrackName()); //Add new stuff when the system is ready
						List<String> aquep = new ArrayList<String>();
						aquep.addAll(arena.getPlayers());
						for(String name:aquep){
						game.join(name);
						arena.removePlayer(name);
						}
						arena.setTransitioning(false);
						plugin.raceQues.setQue(aname, arena);
						startGame(arena, aname, game);
						return;
					}}, grace); //10 seconds
				
			}
		}
		return;
	}
	public void startGame(RaceQue que, String trackName, final Race race){
		this.games.put(race.getGameId(), race);
		final List<String> players = race.getPlayers();
		Map<String, ItemStack[]> oldInv = new HashMap<String,ItemStack[]>();
			for(String player:players){
				Player pl = plugin.getServer().getPlayer(player);
				oldInv.put(player,pl.getInventory().getContents());
				pl.getInventory().clear();
				pl.setGameMode(GameMode.SURVIVAL);
				
			}
		final ArrayList<Minecart> cars = new ArrayList<Minecart>();
		race.setOldInventories(oldInv);
		RaceTrack track = race.getTrack();
		ArrayList<SerializableLocation> sgrid = track.getStartGrid();
		HashMap<Integer, Location> grid = new HashMap<Integer, Location>();
		for(int i=0;i<sgrid.size();i++){
			SerializableLocation s = sgrid.get(i);
			grid.put(i, s.getLocation(plugin.getServer()));
		}
		ArrayList<String> assigned = new ArrayList<String>();
		assigned.addAll(players);
		int count = grid.size();
		if(count > assigned.size()){ //If more grid slots than players, only use the right number of grid slots
			count = assigned.size();
		}
		if(assigned.size() > count){
			count = assigned.size(); //Should theoretically never happen but sometimes does?
		}
		for(int i=0;i<count;i++){
		int min = 0;
		int max = assigned.size();
		if(!(max < 1)){
		int randomNumber = random.nextInt(max - min) + min;
		Player p = plugin.getServer().getPlayer(assigned.get(randomNumber));
		assigned.remove(p.getName());
		Location loc = grid.get(i);
		if(p.getVehicle()!=null){
			p.getVehicle().eject();
		}
		p.teleport(loc.add(0, 2, 0));
		Minecart car = (Minecart) loc.getWorld().spawnEntity(loc.add(0, 0.2, 0), EntityType.MINECART);
		car.setMetadata("car.frozen", new StatValue(null, main.plugin));
		car.setMetadata("kart.racing", new StatValue(null, main.plugin));
		car.setPassenger(p);
		p.setMetadata("car.stayIn", new StatValue(null, plugin));
		cars.add(car);
		}
		}
		if(assigned.size() > 0){
			Player p = plugin.getServer().getPlayer(assigned.get(0));
			p.sendMessage(main.colors.getError()+main.msgs.get("race.que.full"));
			race.leave(p.getName(), true);
		}
		final Map<String, Location> locations = new HashMap<String, Location>();
		for(String name:players){
			locations.put(name, plugin.getServer().getPlayer(name).getLocation());
			plugin.getServer().getPlayer(name).sendMessage(main.colors.getInfo()+main.msgs.get("race.que.preparing"));
		}
		List<String> gameIn = new ArrayList<String>();
		gameIn.addAll(race.getPlayers());
		race.setInPlayers(gameIn);
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			public void run() {
				for(String name:players){
					Player p=plugin.getServer().getPlayer(name);
					p.sendMessage(main.colors.getInfo()+main.msgs.get("race.que.starting"));
				}
				for(int i=10;i>0;i--){
				if(i==10){
					Player p=plugin.getServer().getPlayer(players.get(0));
					p.getLocation().getWorld().playSound(p.getLocation(), Sound.BREATH, 8, 1);
				}
				if(i==3){
					Player p=plugin.getServer().getPlayer(players.get(0));
					p.getLocation().getWorld().playSound(p.getLocation(), Sound.NOTE_BASS_DRUM, 8, 1);
				}
				for(String name:players){
				Player p=plugin.getServer().getPlayer(name);
				p.sendMessage(main.colors.getInfo()+""+i);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				}
				for(Minecart car:cars){
					car.removeMetadata("car.frozen", main.plugin);
				}
				for(String name:players){
					Player p=plugin.getServer().getPlayer(name);
					p.sendMessage(main.colors.getInfo()+main.msgs.get("race.que.go"));
					}
				race.start();
				return;
			}});
		
		return;
	}
	public void updateGame(Race game){
		this.games.put(game.getGameId(), game);
		return;
	}
	public void stopGame(RaceTrack track, String gameId) throws Exception{
		if(!trackInUse(track.getTrackName())){
			return;
		}
		removeRace(track.getTrackName());
		reCalculateQues();
		return;
	}
	public void leaveQue(String playername, RaceQue arena, String arenaName){
		if(getQue(arena).contains(playername)){
			arena.removePlayer(playername);
		}
		for(String ppname:getQue(arena)){
			if(plugin.getServer().getPlayer(ppname).isOnline() && plugin.getServer().getPlayer(ppname) != null){
				plugin.getServer().getPlayer(ppname).sendMessage(main.colors.getTitle()+"[MarioKart:] "+main.colors.getInfo()+playername+main.msgs.get("race.que.left"));
			}
		}
		reCalculateQues();
		return;
	}
	public List<String> getQue(RaceQue que){
		return que.getPlayers();
	}
    public Boolean trackInUse(String arenaName){
    	Set<String> keys = this.games.keySet();
    	ArrayList<String> kz = new ArrayList<String>();
    	kz.addAll(keys);
    	for(String key:kz){
    		Race game = this.games.get(key);
    		if(game.getTrackName().equalsIgnoreCase(arenaName)){
    			if(!game.running){
    				removeRace(game.getTrackName());
    				this.games.remove(key);
    			}
    			else{
    			return true;
    			}
    		}
    	}
    	return false;
    }
    public Boolean removeRace(String trackName){
    	Set<String> keys = this.games.keySet();
    	for(String key:keys){
    		Race game = this.games.get(key);
    		if(game.getTrackName().equalsIgnoreCase(trackName)){
    			for(String p:game.getPlayers()){
    				Player pl = plugin.getServer().getPlayer(p);
    				pl.removeMetadata("car.stayIn", plugin);
    			}
    			this.games.remove(key);
    		}
    	}
    	return false;
    }
    public HashMap<String, Race> getGames(){
    	return this.games;
    }
}
