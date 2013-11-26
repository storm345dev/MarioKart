package net.stormdev.mario.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

public final class SerializableLocation implements Serializable {
	private static final long serialVersionUID = 8650311534439769069L;

	private final String world;
	private final String uuid;
	private final double x, y, z;
	private final float yaw, pitch;
	private transient Location loc;

	public SerializableLocation(Location l) {
		this.world = l.getWorld().getName();
		this.uuid = l.getWorld().getUID().toString();
		this.x = l.getX();
		this.y = l.getY();
		this.z = l.getZ();
		this.yaw = l.getYaw();
		this.pitch = l.getPitch();
	}

	public static Location returnLocation(SerializableLocation l) {
		float pitch = l.pitch;
		float yaw = l.yaw;
		double x = l.x;
		double y = l.y;
		double z = l.z;
		World world = Bukkit.getWorld(l.world);
		Location location = new Location(world, x, y, z, yaw, pitch);
		return location;
	}

	public SerializableLocation(Map<String, Object> map) {
		this.world = (String) map.get("world");
		this.uuid = (String) map.get("uuid");
		this.x = (Double) map.get("x");
		this.y = (Double) map.get("y");
		this.z = (Double) map.get("z");
		this.yaw = ((Float) map.get("yaw")).floatValue();
		this.pitch = ((Float) map.get("pitch")).floatValue();
	}

	public final Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("world", this.world);
		map.put("uuid", this.uuid);
		map.put("x", this.x);
		map.put("y", this.y);
		map.put("z", this.z);
		map.put("yaw", this.yaw);
		map.put("pitch", this.pitch);
		return map;
	}

	public final Location getLocation(Server server) {
		if (loc == null) {
			World world = server.getWorld(this.uuid);
			if (world == null) {
				world = server.getWorld(this.world);
			}
			loc = new Location(world, x, y, z, yaw, pitch);
		}
		return loc;
	}
}
