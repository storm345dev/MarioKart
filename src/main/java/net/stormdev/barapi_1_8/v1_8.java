package net.stormdev.barapi_1_8;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.stormdev.util.MetadataValue;
import org.stormdev.util.Subtitle;
import org.stormdev.util.Versioning;

/**
* This is the FakeDragon class for BarAPI.
* It is based on the code by SoThatsIt.
*
* http://forums.bukkit.org/threads/tutorial-utilizing-the-boss-health-bar.158018/page-5#post-2053705
*
* @author James Mortemore
*/

public class v1_8 extends FakeDragon {
	private Object dragon;
	private int id;
	private static final String DRAGON_META = "bossBar";
	private static final String BOSS_TEXT_META = "bossTextMeta";

	public static void setDragon(Player player, FakeDragon dragon){
		BarAPI1_8.players.put(player.getUniqueId(), dragon);
		if(player.hasMetadata(DRAGON_META)){
			clearDragon(player);
		}
		player.setMetadata(DRAGON_META, new MetadataValue(dragon, MarioKart.plugin));
	}
	
	public static void start1_8PacketHandling(){
		/*
		Bukkit.getScheduler().runTaskTimerAsynchronously(BarAPI.plugin, new Runnable(){

			@Override
			public void run() {
				for(Player player: new ArrayList<Player>(Arrays.asList(Bukkit.getOnlinePlayers()))){
					if(player.hasMetadata(DRAGON_META)){
						try {
							String prevMsg = "";
							if(player.hasMetadata(BOSS_TEXT_META)){
								try {
									Object o = player.getMetadata(BOSS_TEXT_META).get(0).value();
									prevMsg = o.toString();
								} catch (Exception e) {
									player.removeMetadata(BOSS_TEXT_META, BarAPI.plugin);
								}
							}
							
							FakeDragon dragon = (FakeDragon) player.getMetadata(DRAGON_META).get(0).value();
							if(dragon.name != prevMsg){
								player.removeMetadata(BOSS_TEXT_META, BarAPI.plugin);
								player.setMetadata(BOSS_TEXT_META, new MetadataValue(dragon.name, BarAPI.plugin));
								player.sendMessage(ChatColor.GRAY+"[INFO] "+dragon.name);
							}
						} catch (Exception e) {
							clearDragon(player);
						}
					}
				}
				return;
			}}, 1l, 1l);
			*/
		Bukkit.getScheduler().runTaskTimerAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				
				for(Player player: new ArrayList<Player>(Bukkit.getOnlinePlayers())){
					if(!Versioning.is1_8(player)){
						continue; //Don't do anything
					}
						
					if(BarAPI1_8.players.containsKey(player.getUniqueId())){
						try {
							FakeDragon dragon = (FakeDragon) BarAPI1_8.players.get(player.getUniqueId());
							String msg = dragon.name;
							Subtitle.sendSubtitle(player, msg);
						} catch (Exception e) {
							clearDragon(player);
						}
					}
				}
				return;
			}}, 1l, 1l);
	}
	
	public static void clearDragon(Player player){
		player.removeMetadata(DRAGON_META, MarioKart.plugin);
	}
	
	public v1_8(String name, Location loc) {
		super(name, loc);
	}

	@Override
	public Object getSpawnPacket() {
		Class<?> Entity = Util.getCraftClass("Entity");
		Class<?> EntityLiving = Util.getCraftClass("EntityLiving");
		Class<?> EntityEnderDragon = Util.getCraftClass("EntityEnderDragon");
		Object packet = null;
		try {
			dragon = EntityEnderDragon.getConstructor(Util.getCraftClass("World")).newInstance(getWorld());
			
			Method setLocation = Util.getMethod(EntityEnderDragon, "setLocation", new Class<?>[] { double.class, double.class, double.class, float.class, float.class });
			setLocation.invoke(dragon, getX(), getY(), getZ(), getPitch(), getYaw());
			
			Method setInvisible = Util.getMethod(EntityEnderDragon, "setInvisible", new Class<?>[] { boolean.class });
			setInvisible.invoke(dragon, isVisible());

			Method setCustomName = Util.getMethod(EntityEnderDragon, "setCustomName", new Class<?>[] { String.class });
			setCustomName.invoke(dragon, name);

			Method setHealth = Util.getMethod(EntityEnderDragon, "setHealth", new Class<?>[] { float.class });
			setHealth.invoke(dragon, health);

			Field motX = Util.getField(Entity, "motX");
			motX.set(dragon, getXvel());

			Field motY = Util.getField(Entity, "motY");
			motY.set(dragon, getYvel());

			Field motZ = Util.getField(Entity, "motZ");
			motZ.set(dragon, getZvel());

			Method getId = Util.getMethod(EntityEnderDragon, "getId", new Class<?>[] {});
			this.id = (Integer) getId.invoke(dragon);

			Class<?> PacketPlayOutSpawnEntityLiving = Util.getCraftClass("PacketPlayOutSpawnEntityLiving");

			packet = PacketPlayOutSpawnEntityLiving.getConstructor(new Class<?>[] { EntityLiving }).newInstance(dragon);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getDestroyPacket() {
		Class<?> PacketPlayOutEntityDestroy = Util.getCraftClass("PacketPlayOutEntityDestroy");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityDestroy.newInstance();
			Field a = PacketPlayOutEntityDestroy.getDeclaredField("a");
			a.setAccessible(true);
			a.set(packet, new int[] { id });
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getMetaPacket(Object watcher) {
		Class<?> DataWatcher = Util.getCraftClass("DataWatcher");

		Class<?> PacketPlayOutEntityMetadata = Util.getCraftClass("PacketPlayOutEntityMetadata");
		
		Object packet = null;
		try {
			packet = PacketPlayOutEntityMetadata.getConstructor(new Class<?>[] { int.class, DataWatcher, boolean.class }).newInstance(id, watcher, true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getTeleportPacket(Location loc) {
		Class<?> PacketPlayOutEntityTeleport = Util.getCraftClass("PacketPlayOutEntityTeleport");
		
		Object packet = null;
		
		try {
			packet = PacketPlayOutEntityTeleport.getConstructor(new Class<?>[] { int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class }).newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360), isVisible());
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			//e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			//e.printStackTrace();
		} catch (NoSuchMethodException e) {
			//e.printStackTrace();
		}
		
		if(packet == null){
			try {
				packet = PacketPlayOutEntityTeleport.getConstructor(new Class<?>[] { int.class, int.class, int.class, int.class, byte.class, byte.class }).newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				try {
					packet = PacketPlayOutEntityTeleport.getConstructor(new Class<?>[] { int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class, boolean.class }).newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360), isVisible(), true);
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}
			}
		}

		return packet;
	}

	@Override
	public Object getWatcher() {
		Class<?> Entity = Util.getCraftClass("Entity");
		Class<?> DataWatcher = Util.getCraftClass("DataWatcher");

		Object watcher = null;
		try {
			watcher = DataWatcher.getConstructor(new Class<?>[] { Entity }).newInstance(dragon);
			Method a = Util.getMethod(DataWatcher, "a", new Class<?>[] { int.class, Object.class });

			a.invoke(watcher, 0, isVisible() ? (byte) 0 : (byte) 0x20);
			a.invoke(watcher, 6, (Float) health);
			a.invoke(watcher, 7, (Integer) 0);
			a.invoke(watcher, 8, (Byte) (byte) 0);
			a.invoke(watcher, 10, name);
			a.invoke(watcher, 11, (Byte) (byte) 1);
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (SecurityException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		} catch (InvocationTargetException e) {

			e.printStackTrace();
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}
		return watcher;
	}
}
