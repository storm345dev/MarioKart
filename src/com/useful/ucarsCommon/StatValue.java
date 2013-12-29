package com.useful.ucarsCommon;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class StatValue implements MetadataValue {
	public Object value = null;
	public Plugin plugin = null;

	public StatValue(Object value, Plugin plugin) {
		this.value = value;
		this.plugin = plugin;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
		return;
	}

	@Override
	public boolean asBoolean() {
		return false;
	}

	@Override
	public byte asByte() {
		return 0;
	}

	@Override
	public double asDouble() {
		return 0;
	}

	@Override
	public float asFloat() {
		return 0;
	}

	@Override
	public int asInt() {
		return 0;
	}

	@Override
	public long asLong() {
		return 0;
	}

	@Override
	public short asShort() {
		return 0;
	}

	@Override
	public String asString() {
		return null;
	}

	@Override
	public Plugin getOwningPlugin() {
		return plugin;
	}

	@Override
	public void invalidate() {
		return;
	}

	@Override
	public Object value() {
		return value;
	}
}
