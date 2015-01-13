package net.stormdev.barapi_1_8;

import java.lang.reflect.Method;

import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.entity.Player;

public class Subtitle {
	public static void sendSubtitle(Player player, String message){ //Sends to their chat if 1.7, not 1.8
		try {
			Class<?> IChatBaseComponent = Util.getCraftClass("IChatBaseComponent");
			Class<?> PacketPlayOutChat = Util.getCraftClass("PacketPlayOutChat");
			Class<?> ChatSerializer = Util.getCraftClass("ChatSerializer");
			Method aChatSerializer = ChatSerializer.getDeclaredMethod("a", String.class);
			
			Object o = aChatSerializer.invoke(null, new Gson().toJson(message));
			Object packet = PacketPlayOutChat.getConstructor(IChatBaseComponent, byte.class).newInstance(IChatBaseComponent.cast(o), (byte)2);
			Util.sendPacket(player, packet);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
