package net.stormdev.mario.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener implements Listener {
	private FullServerManager fsm;
	public ServerListener(){
		this.fsm = FullServerManager.get();
	}
	
	
	@EventHandler
	void onPing(ServerListPingEvent event){
		event.setMotd(fsm.getMOTD());
	}
	
	
	@EventHandler
	void playerJoin(PlayerJoinEvent event){
		final Player player = event.getPlayer();
		
		
		//Enable resource pack for them:
		String rl = MarioKart.plugin.packUrl;                           //Send them the download url, etc for if they haven't get server RPs enabled
		player.sendMessage(MarioKart.colors.getInfo()
				+ MarioKart.msgs.get("resource.download"));
		String msg = MarioKart.msgs.get("resource.downloadHelp");
		msg = msg.replaceAll(Pattern.quote("%url%"),
				Matcher.quoteReplacement(ChatColor.RESET + ""));
		player.sendMessage(MarioKart.colors.getInfo() + msg);
		player.sendMessage(rl); //new line
		
		if(!MarioKart.plugin.resourcedPlayers.contains(player.getName()) //Send them the RP for if they have got server RPs enabled
				&& MarioKart.plugin.fullPackUrl != null
				&& MarioKart.plugin.fullPackUrl.length() > 0){
			player.setTexturePack(MarioKart.plugin.fullPackUrl);
			MarioKart.plugin.resourcedPlayers.add(player.getName());
		}
	}
}
