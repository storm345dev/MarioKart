package net.stormdev.ucars.race;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.stormdev.mariokartAddons.KartAction;
import net.stormdev.ucars.utils.CheckpointCheck;
import net.stormdev.ucars.utils.DoubleValueComparator;
import net.stormdev.ucars.utils.RaceEndEvent;
import net.stormdev.ucars.utils.RaceFinishEvent;
import net.stormdev.ucars.utils.RaceQue;
import net.stormdev.ucars.utils.RaceStartEvent;
import net.stormdev.ucars.utils.RaceUpdateEvent;
import net.stormdev.ucars.utils.TrackCreator;
import net.stormdev.ucars.utils.shellUpdateEvent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.useful.ucars.ItemStackFromId;
import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class URaceListener implements Listener {
	main plugin = null;
	public URaceListener(main plugin){
		this.plugin = plugin;
	}
	public void penalty(final Minecart car, long time){
		if(car == null){
			return;
		}
		if(car.hasMetadata("kart.immune")){
			return;
		}
		double power = (time/2);
		if(power < 1){
			power = 1;
		}
		car.setMetadata("car.frozen", new StatValue(time, plugin));
		car.setVelocity(new Vector(0,power,0));
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

			public void run() {
				car.getLocation().getWorld().playSound(car.getLocation(), Sound.WOOD_CLICK, 1f, 1f);
				car.removeMetadata("car.frozen", plugin);
			}}, (time*20));
		return;
	}
	@EventHandler
	void bananas(PlayerPickupItemEvent event){
		Item item = event.getItem();
		ItemStack stack = item.getItemStack();
		Player player = event.getPlayer();
		if(!ucars.listener.inACar(player)){
			return;
		}
		if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
		if(ItemStackFromId.equals(main.config.getString("mariokart.banana"), stack.getTypeId(), stack.getDurability())){
			player.getWorld().playSound(player.getLocation(), Sound.SPLASH2, 1f, 0.5f);
			item.remove();
			this.penalty(((Minecart)player.getVehicle()), 1);
			event.setCancelled(true);
			return;
		}
		return;
	}
	@EventHandler
	public void onWandClickEvent(PlayerInteractEvent event){
		if(!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			return;
		}
		Player player = event.getPlayer();
		if(!main.trackCreators.containsKey(player.getName())){
			return;
		}
		TrackCreator creator = main.trackCreators.get(player.getName());
		Boolean wand = false;
		int handid = player.getItemInHand().getTypeId();
		if(handid == main.config.getInt("setup.create.wand")){
			wand = true;
		}
		creator.set(wand);
		return;
	}
	@EventHandler (priority = EventPriority.MONITOR)
	void powerups(ucarUpdateEvent event){
		Player player = (Player) event.getVehicle().getPassenger();
		try {
			if(plugin.raceMethods.inAGame(player.getName())==null){
				return;
			}
		} catch (Exception e) {
			return;
		}
	    main.marioKart.calculate(player, event);
	    return;
	}
	@EventHandler (priority = EventPriority.LOWEST)
	void trackingShells(shellUpdateEvent event){
		//if target is null then green shell
		final Entity shell = event.getShell();
		Location shellLoc = shell.getLocation();
		int sound = 0;
        if(shell.hasMetadata("shell.sound")){
			sound = (Integer) ((StatValue)shell.getMetadata("shell.sound").get(0)).getValue();
		}
        if(sound < 1){
        	shellLoc.getWorld().playSound(shellLoc, Sound.NOTE_PLING, 1.25f, 1.8f);
        	sound = 3;
        	shell.removeMetadata("shell.sound", plugin);
        	shell.setMetadata("shell.sound", new StatValue(sound, plugin));
        }
        else{
        	sound--;
        	shell.removeMetadata("shell.sound", plugin);
        	shell.setMetadata("shell.sound", new StatValue(sound, plugin));
        }
		double speed = 1.2;
				String targetName = event.getTarget();
				if(targetName != null){
					final Player target = plugin.getServer().getPlayer(targetName);
					Location targetLoc = target.getLocation();
					double x = targetLoc.getX()-shellLoc.getX();
					double z = targetLoc.getZ()-shellLoc.getZ();
					Boolean ux = true;
					double px = Math.abs(x);
					double pz = Math.abs(z);
					if(px > pz){
						ux = false;
					}
					
					if(ux){
						//x is smaller
						//long mult = (long) (pz/speed);
						x = (x/pz)*speed;
						z = (z/pz)*speed;
					}
					else{
						//z is smaller
						//long mult = (long) (px/speed);
						x = (x/px)*speed;
						z = (z/px)*speed;
					}
					Vector vel = new Vector(x, 0, z);
					shell.setVelocity(vel);
					if(pz < 1.1 && px < 1.1){
								String msg = main.msgs.get("mario.hit");
								msg = msg.replaceAll(Pattern.quote("%name%"), "tracking shell");
								target.getLocation().getWorld().playSound(target.getLocation(), Sound.ENDERDRAGON_HIT, 1, 0.8f);
								target.sendMessage(ChatColor.RED+msg);
								penalty(((Minecart)target.getVehicle()), 4);
								shell.setMetadata("shell.destroy", new StatValue(0, plugin));
								return;
					}
				    return;
				}
				else{
					speed = 1.5;
					Vector direction = event.direction;
					if(!event.getCooldown()){
					if(shellLoc.getBlock().getType() != Material.AIR && shellLoc.getBlock().getType() != Material.CARPET){
						//Bounce
						direction = direction.multiply(-1);
					}
					}
					shell.setVelocity(direction);
					if(!event.getCooldown()){
					 if(shell.getNearbyEntities(2, 2, 2).size() > 0){
						List<Entity> nearby = shell.getNearbyEntities(2, 2, 2);
						for(Entity entity:nearby){
							if(entity instanceof Player){
								Player pl = (Player) entity;
								if(ucars.listener.inACar(pl)){
								String msg = main.msgs.get("mario.hit");
								msg = msg.replaceAll(Pattern.quote("%name%"), "green shell");
								pl.getLocation().getWorld().playSound(pl.getLocation(), Sound.ENDERDRAGON_HIT, 1, 0.8f);
								pl.sendMessage(ChatColor.RED+msg);
								penalty(((Minecart)pl.getVehicle()), 4);
								shell.setMetadata("shell.destroy", new StatValue(0, plugin));
								}
								return;
							}
						}
					}
				  }
				}
				
		return;
	}
	@EventHandler (priority = EventPriority.HIGHEST)
	void RaceEnd(RaceEndEvent event){
		Race game = event.getRace();
		game.running = false;
		if(plugin.gameScheduler.trackInUse(game.getTrackName())){
			plugin.gameScheduler.removeRace(game.getTrackName());
		}
		try {
			plugin.gameScheduler.stopGame(game.getTrack(), game.getGameId());
		} catch (Exception e) {
		}
		plugin.gameScheduler.reCalculateQues();
	}
	
	//Much extranious PORTED code from here on (Races)
	@EventHandler (priority = EventPriority.HIGHEST)
	void RaceEnd(RaceFinishEvent event){
		Race game = event.getRace();
		List<String> players = new ArrayList<String>();
		players.addAll(game.getPlayers());
		List<String> inplayers = game.getInPlayers();
		String in = "";
		for(String inp:inplayers){
			in = in+", "+inp; 
		}
		Map<String,Double> scores = new HashMap<String,Double>();
		Boolean finished = false;
		String playername = event.getPlayername();
				final Player player = plugin.getServer().getPlayer(playername);
				player.removeMetadata("car.stayIn", plugin);
				player.setCustomName(ChatColor.stripColor(player.getCustomName()));
				player.setCustomNameVisible(false);
				if(player.getVehicle()!=null){
					Vehicle veh = (Vehicle) player.getVehicle();
					veh.eject();
					veh.remove();
				}
				Location loc = game.getTrack().getExit(plugin.getServer());
				if(loc == null){
				player.teleport(player.getLocation().getWorld().getSpawnLocation());
				}
				else{
					player.teleport(loc);
				}
				if(player.isOnline()){
						player.getInventory().clear();
						if(game.getOldInventories().containsKey(player.getName())){
							player.getInventory().setContents(game.getOldInventories().get(player.getName()));
						}
				}
				if(game.finished.contains(playername)){
					finished = true;
				}
				else{
					HashMap<String, Double> checkpointDists = new HashMap<String, Double>();
					for(String pname:game.getPlayers()){
						Player pp = main.plugin.getServer().getPlayer(pname);
						if(pp.hasMetadata("checkpoint.distance")){
							List<MetadataValue> metas = pp.getMetadata("checkpoint.distance");
							checkpointDists.put(pname, (Double) ((StatValue)metas.get(0)).getValue());
						}
					}
				for(String pname:game.getPlayers()){
				int laps = game.totalLaps - game.lapsLeft.get(pname) +1;
				int checkpoints;
				try {
					checkpoints = game.checkpoints.get(pname);
				} catch (Exception e) {
					checkpoints = 0;
				}
				double distance = 1/(checkpointDists.get(pname));
				double score = (laps*game.getMaxCheckpoints()) + checkpoints + distance;
				try {
					if(game.getWinner().equals(pname)){
						score = score+1;
					}
				} catch (Exception e) {
				}
				scores.put(pname, score);
				}
				}
				player.getInventory().clear();
				if(game.getOldInventories().containsKey(player.getName())){
				player.getInventory().setContents(game.getOldInventories().get(player.getName()));
				}
		if(!finished){
		DoubleValueComparator com = new DoubleValueComparator(scores);
    	SortedMap<String, Double> sorted = new TreeMap<String, Double>(com);
		sorted.putAll(scores);
    	Set<String> keys = sorted.keySet();
		Object[] pls = (Object[]) keys.toArray();
    	for(int i=0;i<pls.length;i++){
			Player p = plugin.getServer().getPlayer((String) pls[i]);
			if(p.getName().equals(event.getPlayername())){
			if(p!=null){
				String msg = main.msgs.get("race.end.position");
				if((i+1) <=4 && (i+1) != game.getPlayers().size()){
					player.getWorld().playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1, 1);
				}
				else{
					player.getWorld().playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
				}
				String pos = ""+(i+1);
				if(pos.endsWith("1")){
					pos = pos+"st";
				}
				else if(pos.endsWith("2")){
					pos = pos+"nd";
				}
				else if(pos.endsWith("3")){
					pos = pos+"rd";
				}
				else {
					pos = pos+"th";
				}
				msg = msg.replaceAll("%position%", ""+pos);
				p.sendMessage(main.colors.getSuccess()+msg);
			}
			}
    	}
		}
		else{
			Player p = plugin.getServer().getPlayer((String) event.getPlayername());
			if(p!=null){
			int position = 1;
			ArrayList<String> fs = new ArrayList<String>();
			fs.addAll(game.finished);
			for(int i=0;i<fs.size();i++){
				if(fs.get(i).equals(event.getPlayername())){
					position = i+1;
				}
			}
			String msg = main.msgs.get("race.end.position");
			if(position <=4 && position != game.getPlayers().size()){
				player.getWorld().playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1, 1);
			}
			else{
				player.getWorld().playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
			}
			String pos = ""+position;
			if(pos.endsWith("1")){
				pos = pos+"st";
			}
			else if(pos.endsWith("2")){
				pos = pos+"nd";
			}
			else if(pos.endsWith("3")){
				pos = pos+"rd";
			}
			else {
				pos = pos+"th";
			}
			try {
				msg = msg.replaceAll("%position%", ""+pos);
			} catch (Exception e) {
			}
			p.sendMessage(main.colors.getSuccess()+msg);
			}
		}
		game.leave(event.getPlayername(), false);
		plugin.gameScheduler.updateGame(game);
		if(game.getInPlayers().size() < 1){
			game.ended = true;
			game.end();
		}
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

			public void run() {
				String rl = main.config.getString("mariokart.resourceNonMarioPack");
				Boolean valid = true;
				try {
					new URL(rl);
				} catch (MalformedURLException e2) {
					valid = false;
				}
				if(valid){
					player.sendMessage(main.colors.getInfo()+main.msgs.get("resource.clear"));
					player.setTexturePack(rl);
				}
				return;
			}}, 150l);
		return;
	}
	@EventHandler
	void gameQuitting(PlayerQuitEvent event){
		Player player = event.getPlayer();
		Race game = plugin.raceMethods.inAGame(player.getName());
		if(game == null){
			String arenaName = plugin.raceMethods.inGameQue(player.getName());
			if(arenaName == null){
				return;
			}
			RaceQue arena = plugin.raceQues.getQue(arenaName);
			arena.removePlayer(player.getName());
			plugin.raceQues.setQue(arenaName, arena);
			return;
		}
		else{
			game.leave(player.getName(), true);
			return;
	    }
	}
	@EventHandler
	void gameQuitting(PlayerKickEvent event){
		Player player = event.getPlayer();
		Race game = plugin.raceMethods.inAGame(player.getName());
		if(game == null){
			String arenaName = plugin.raceMethods.inGameQue(player.getName());
			if(arenaName == null){
				return;
			}
			RaceQue arena = plugin.raceQues.getQue(arenaName);
			arena.removePlayer(player.getName());
			plugin.raceQues.setQue(arenaName, arena);
			return;
		}
		else{
			game.leave(player.getName(), true);
			return;
	    }
	}
	@EventHandler
	void stayInCar(VehicleExitEvent event){
		if(!(event.getVehicle() instanceof Minecart)){
			return;
		}
		Minecart car = (Minecart) event.getVehicle();
		if(!(event.getExited() instanceof Player)){
			return;
		}
		Player player = (Player) event.getExited();
		if(!(player.hasMetadata("car.stayIn"))){
			return;
		}
		if(!ucars.listener.isACar(car)){
			return;
		}
		event.setCancelled(true);
	}
	@EventHandler (priority = EventPriority.HIGHEST)
	void RaceStart(RaceStartEvent event){
		Race game = event.getRace();
		List<String> players = game.getPlayers();
		for(String pname:players){
			plugin.getServer().getPlayer(pname).setGameMode(GameMode.SURVIVAL);
			plugin.getServer().getPlayer(pname).getInventory().clear();
		}
			for(int i=0;i<game.getPlayers().size();i++){
				String pname = game.getPlayers().get(i);
				plugin.gameScheduler.updateGame(game);
				if(game.lapsLeft.containsKey(pname)){
					game.lapsLeft.put(pname, game.totalLaps);
				}
				if(game.checkpoints.containsKey(pname)){
					game.checkpoints.put(pname, 0);
				}
				String msg = main.msgs.get("race.mid.lap");
				msg = msg.replaceAll(Pattern.quote("%lap%"), ""+1);
				msg = msg.replaceAll(Pattern.quote("%total%"), ""+game.totalLaps);
			    plugin.getServer().getPlayer(pname).sendMessage(main.colors.getInfo()+msg);
			}
		plugin.gameScheduler.reCalculateQues();
		return;
	}
	@EventHandler
	void RaceHandler(RaceUpdateEvent event){
		final Race game = event.getRace();
		if(!game.getRunning()){
			try {
				plugin.gameScheduler.stopGame(game.getTrack(), game.getTrackName());
			} catch (Exception e) {
			}
			plugin.gameScheduler.reCalculateQues();
			return;
		}
		ArrayList<String> pls = new ArrayList<String>();
		pls.addAll(game.getInPlayers());
		for(String playername:pls){
			Player player = plugin.getServer().getPlayer(playername);
			if(player == null){
				game.leave(playername, true);
			}
			else{
			Location playerLoc = player.getLocation();
			Boolean checkNewLap = false;
			int old = 0;
			try {
				old = game.checkpoints.get(playername);
			} catch (Exception e) {
				old = 0;
			}
			if(old == game.getMaxCheckpoints()){
				checkNewLap = true;
			}
			Integer[] toCheck = new Integer[]{};
			if(checkNewLap){
				toCheck = new Integer[]{0};
			}
			else{
				toCheck = new Integer[]{(old+1)};
			}
			CheckpointCheck check = game.playerAtCheckpoint(toCheck, player, plugin.getServer());
			
			if(check.at){ //At a checkpoint
				int ch = check.checkpoint;
				if(ch >=game.getMaxCheckpoints()){
					checkNewLap = true;
				}
				if(!(ch == old)){
				/* Removed to reduce server load - Requires all checkpoints to be checked
				if(ch-2 > old){
					//They missed a checkpoint
					player.sendMessage(main.colors.getError()+main.msgs.get("race.mid.miss"));
					return;
				}
				*/
				if(!(old>=ch)){
					game.checkpoints.put(playername, check.checkpoint);
				}
			  }
			}
			int lapsLeft = 3;
			try {
				lapsLeft = game.lapsLeft.get(playername);
			} catch (Exception e) {
				game.lapsLeft.put(playername, game.totalLaps);
				lapsLeft = game.totalLaps;
			}
			if(lapsLeft < 1 || checkNewLap){
				if(game.atLine(plugin.getServer(), playerLoc)){
					if(checkNewLap){
						int left = game.lapsLeft.get(playername)-1;
						if(left < 0){
							left = 0;
						}
						game.checkpoints.put(playername, 0);
						game.lapsLeft.put(playername, left);
						lapsLeft = left;
						if(left != 0){
							String msg = main.msgs.get("race.mid.lap");
							int lap = game.totalLaps - lapsLeft+1;
							msg = msg.replaceAll(Pattern.quote("%lap%"), ""+lap);
							msg = msg.replaceAll(Pattern.quote("%total%"), ""+game.totalLaps);
							if(lap == game.totalLaps){
								player.getWorld().playSound(player.getLocation(), Sound.NOTE_STICKS, 2, 1);
							}
						    player.sendMessage(main.colors.getInfo()+msg);
						}
					}
					if(lapsLeft < 1){
					Boolean won = game.getWinner() == null;
					if(won){
					game.setWinner(playername);
					}
					game.finish(playername);
					if(won){
					ArrayList<String> plz = new ArrayList<String>();
					plz.addAll(game.getPlayers());
					for(String pname:plz){
						if(!(plugin.getServer().getPlayer(pname) == null) && !playername.equals(pname)){
							String msg = main.msgs.get("race.end.soon");
							msg = msg.replaceAll("%name%", playername);
							plugin.getServer().getPlayer(pname).sendMessage(main.colors.getSuccess()+game.getWinner()+main.msgs.get("race.end.won"));
							plugin.getServer().getPlayer(pname).sendMessage(main.colors.getInfo()+msg);
						}
					}
					}
					}
				}
			  }
			}
		}
		plugin.gameScheduler.updateGame(game);
		return;
	}
	@EventHandler
	void damage(EntityDamageEvent event){
		if(!(event.getEntityType() == EntityType.MINECART)){
		    return;	
		}
		if(!(event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION)){
			return;
		}
		if(!ucars.listener.isACar((Minecart) event.getEntity())){
			return;
		}
		try {
			if(plugin.raceMethods.inAGame(((Player) event.getEntity().getPassenger()).getName()) == null && !(event.getEntity().hasMetadata("kart.immune"))){
				return;
			}
		} catch (Exception e) {
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
	}
	@EventHandler
	void exploder(EntityExplodeEvent event){
		if(!main.config.getBoolean("mariokart.enable")){
			return;
		}
		if(event.getEntity() == null){
			return;
		}
		if(event.getEntity().hasMetadata("explosion.none")){
			Location loc = event.getEntity().getLocation();
			event.setCancelled(true);
			event.getEntity().remove();
			double radius = 6;
			loc.getWorld().createExplosion(loc, 0);
			Double x = (double) radius;
			Double y = (double) radius;
			Double z = (double) radius;
			List<Entity> near = event.getEntity().getNearbyEntities(x, y, z);
			
			Object[] entarray = near.toArray();
			
				Entity listent;
				
				

				for (Object s  : entarray)
				{
				    listent = (Entity) s;
				    EntityType type = listent.getType();
				    if(type == EntityType.MINECART){
				    	if(ucars.listener.isACar((Minecart) listent)){
				    		((Minecart) listent).setDamage(0);
				    		penalty((Minecart) listent, 4);
				    	}
				    }
				}
				
		}
	}
	@EventHandler
	void signClicker(PlayerInteractEvent event){
		main.marioKart.calculate(event.getPlayer(), event);
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
			return;
		}
		if(!(event.getClickedBlock().getState() instanceof Sign)){
			return;
		}
		Sign sign = (Sign) event.getClickedBlock().getState();
		String[] lines = sign.getLines();
		if(!ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")){
			return;
		}
		String cmd = ChatColor.stripColor(lines[1]);
		if(cmd.equalsIgnoreCase("list")){
			int page = 1;
			try {
				page = Integer.parseInt(ChatColor.stripColor(lines[2]));
			} catch (NumberFormatException e) {
			}
			main.cmdExecutor.urace(event.getPlayer(), new String[]{"list", ""+page}, event.getPlayer());
		}
		else if(cmd.equalsIgnoreCase("leave") || cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")){
			main.cmdExecutor.urace(event.getPlayer(), new String[]{"leave"}, event.getPlayer());
		}
		else if(cmd.equalsIgnoreCase("join")){
			main.cmdExecutor.urace(event.getPlayer(), new String[]{"join", ChatColor.stripColor(lines[2]).toLowerCase()}, event.getPlayer());
		}
		return;
	}
	@EventHandler
	void signWriter(SignChangeEvent event){
		String[] lines = event.getLines();
		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MarioKart]")){
			lines[0] = main.colors.getTitle() + "[MarioKart]";
			Boolean text = true;
			String cmd = ChatColor.stripColor(lines[1]);
			if(cmd.equalsIgnoreCase("list")){
				lines[1] = main.colors.getInfo()+"List";
				if(!(lines[2].length() < 1)){
					text = false;
				}
				lines[2] = main.colors.getSuccess()+ChatColor.stripColor(lines[2]);
			}
			else if(cmd.equalsIgnoreCase("join")){
				lines[1] = main.colors.getInfo()+"Join";
				lines[2] = main.colors.getSuccess()+ChatColor.stripColor(lines[2]);
				if(lines[2].equalsIgnoreCase("auto")){
				   lines[2] = main.colors.getTp() + "Auto";	
				}
				text = false;
			}
			else if(cmd.equalsIgnoreCase("leave") || cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit")){
				char[] raw = cmd.toCharArray();
				if(raw.length > 1){
				String start = ""+raw[0];
				start = start.toUpperCase();
				String body = "";
				for(int i=1;i<raw.length;i++){
					body = body+raw[i];
				}
				body = body.toLowerCase();
				cmd = start+body;
			    }
				lines[1] = main.colors.getInfo() + cmd;
			}
			else if(cmd.equalsIgnoreCase("items")){
				Location above = event.getBlock().getLocation().add(0, 1.4, 0);
				EnderCrystal crystal = (EnderCrystal) above.getWorld().spawnEntity(above, EntityType.ENDER_CRYSTAL);
				above.getBlock().setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.WEST).setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.NORTH).setType(Material.COAL_BLOCK);
				above.getBlock().getRelative(BlockFace.NORTH_WEST).setType(Material.COAL_BLOCK);
				crystal.setFireTicks(0);
				crystal.setMetadata("race.pickup", new StatValue(true, plugin));
				text = false;
			}
			else{
				text = false;
			}
			if(text){
				lines[2] = ChatColor.ITALIC + "Right click";
				lines[3] = ChatColor.ITALIC + "to use";
			}
		}
	}
	@EventHandler
	void crystalExplode(EntityExplodeEvent event){
		if(!(event.getEntity() instanceof EnderCrystal)){
			return;
		}
		Entity crystal = event.getEntity();
		//if(crystal.hasMetadata("race.pickup")){
			event.setCancelled(true);
			event.setYield(0);
			Location newL = crystal.getLocation();
			Location signLoc = null;
			if((newL.add(0, -2.4, 0).getBlock().getState() instanceof Sign)){
				signLoc = newL.add(0, -2.4, 0);
			}
			else{
				return; //Let them destroy it
			}
			Location above = signLoc.add(0, 3.8, 0);
			EnderCrystal newC = (EnderCrystal) above.getWorld().spawnEntity(above, EntityType.ENDER_CRYSTAL);
			above.getBlock().setType(Material.COAL_BLOCK);
			above.getBlock().getRelative(BlockFace.WEST).setType(Material.COAL_BLOCK);
			above.getBlock().getRelative(BlockFace.NORTH).setType(Material.COAL_BLOCK);
			above.getBlock().getRelative(BlockFace.NORTH_WEST).setType(Material.COAL_BLOCK);
			newC.setFireTicks(0);
			newC.setMetadata("race.pickup", new StatValue(true, plugin));
		//}
		
		return;
	}
	public void spawnItemPickupBox(Location previous){
		Location newL = previous;
		Location signLoc = null;
		if((newL.add(0, -2.4, 0).getBlock().getState() instanceof Sign)){
			signLoc = newL.add(0, -2.4, 0);
		}
		else{
			return; //Let them destroy it
		}
		Location above = signLoc.add(0, 3.8, 0);
		EnderCrystal newC = (EnderCrystal) above.getWorld().spawnEntity(above, EntityType.ENDER_CRYSTAL);
		above.getBlock().setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.WEST).setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.NORTH).setType(Material.COAL_BLOCK);
		above.getBlock().getRelative(BlockFace.NORTH_WEST).setType(Material.COAL_BLOCK);
		newC.setFireTicks(0);
		newC.setMetadata("race.pickup", new StatValue(true, plugin));
	}
	@EventHandler (priority = EventPriority.NORMAL)
	void stopCrystalFire(BlockIgniteEvent event){
		if(event.getCause() != IgniteCause.ENDER_CRYSTAL){
			return;
		}
			event.setCancelled(true);
		return;
	}
	@EventHandler (priority = EventPriority.MONITOR)
	void playerFireProtection(EntityDamageEvent event){
		try {
			if(event.getCause() != DamageCause.FIRE && event.getCause() != DamageCause.FIRE_TICK){
				return;
			}
			if(!(event.getEntity() instanceof Player)){
				return;
			}
			if(!ucars.listener.inACar((Player) event.getEntity())){
				return;
			}
			if(plugin.raceMethods.inAGame(((HumanEntity) event.getEntity()).getName()) == null){
				return;
			}
			Player player = ((Player)event.getEntity());
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2, 100));
			double health = player.getHealth();
			health = health + event.getDamage();
			if(health > 20){
				health = 20;
			}
			player.setHealth(health);
			player.setFireTicks(0);
			event.setCancelled(true);
			return;
		} catch (Exception e) {
			//Fire event error - Yes it happens
			return;
		}
	}
	@EventHandler
	void carDeath(VehicleDamageEvent event){
		if(!(event.getVehicle() instanceof Minecart)){
			return;
		}
		if(!ucars.listener.isACar((Minecart) event.getVehicle())){
			return;
		}
		try {
			if(plugin.raceMethods.inAGame(((HumanEntity) event.getVehicle().getPassenger()).getName()) == null){
				return;
			}
		} catch (Exception e) {
			return;
		}
		if(!main.config.getBoolean("mariokart.enable")){
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
		return;
	}
	@EventHandler
	void playerDeathEvent(PlayerDeathEvent event){
		Player player = event.getEntity();
		if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
	    if(!(player.getVehicle() == null)){
		player.getVehicle().eject();
        player.getVehicle().remove();
	    }
	    List<MetadataValue> metas = null;
		if(player.hasMetadata("car.stayIn")){
			metas = player.getMetadata("car.stayIn");
			for(MetadataValue val:metas){
				player.removeMetadata("car.stayIn", val.getOwningPlugin());
			}
		}
	    return;
	}
	@EventHandler
	void playerRespawnEvent(PlayerRespawnEvent event){
		final Player player = event.getPlayer();
		if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
		Race race = plugin.raceMethods.inAGame(player.getName());
		int checkpoint = 0;
		try {
			checkpoint = race.checkpoints.get(player.getName());
		} catch (Exception e) {
		}
		final Location loc = race.getTrack().getCheckpoints().get(checkpoint).getLocation(plugin.getServer()).add(0, 2, 0);
	    plugin.getServer().getScheduler().runTask(plugin, new Runnable(){

			public void run() {
				player.teleport(loc);
				return;
			}});
		Minecart cart = (Minecart) loc.getWorld().spawnEntity(loc, EntityType.MINECART);
	    cart.setPassenger(player);
	    player.setMetadata("car.stayIn", new StatValue(null, plugin));
	    return;
	}
	@EventHandler
	void blockBreak(BlockBreakEvent event){
	    Player player = event.getPlayer();
	    if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
	    event.setCancelled(true);
	    return;
	}
	@EventHandler
	void blockPlace(BlockPlaceEvent event){
	    Player player = event.getPlayer();
	    if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
	    event.setCancelled(true);
	    return;
	}
	@EventHandler (priority = EventPriority.MONITOR)
	void speedo(VehicleUpdateEvent event){
		Entity veh = event.getVehicle();
		if(!(veh instanceof Minecart)){
			return;
		}
		if(!ucars.listener.isACar((Minecart) veh)){
			return;
		}
		Minecart car = (Minecart) veh;
	    Entity pass = car.getPassenger();
	    if(!(pass instanceof Player)){
	    	return;
	    }
	    Player player = (Player) pass;
	    String playername = player.getName();
	    if(plugin.raceMethods.inAGame(playername) == null){
	    	return;
	    }
	    Vector Velocity = car.getVelocity();
	    double speed = (Math.abs(Velocity.getX()) + Math.abs(Velocity.getZ())) * 40;
	    if(speed < 1){
	    	speed = Velocity.getY();
	    }
	    if(speed > 100){
	    	speed = 100;
	    }
	    player.setLevel((int) speed);
	    float xpBar = (float) (speed/100);
	    if(xpBar >= 1){
	    	xpBar = 0.999f;
	    }
	    player.setExp(xpBar);
	    return;
	}
}
