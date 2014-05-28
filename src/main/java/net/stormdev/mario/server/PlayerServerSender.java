package net.stormdev.mario.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerServerSender {
	public static void sendToServer(final Player player, final String serverName){
		Bukkit.getScheduler().runTaskAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
		        DataOutputStream out = new DataOutputStream(b);
		        try {
		            out.writeUTF("Connect");
		            out.writeUTF(serverName);
		        }
		        catch (IOException localIOException1) {
		        }
		        player.sendPluginMessage(MarioKart.plugin, "BungeeCord", b.toByteArray());
		        return;
			}});
	}
}
