package net.stormdev.mario.server;

import org.bukkit.ChatColor;

public enum ServerStage {
WAITING(ChatColor.GREEN+"Waiting", true), STARTING(ChatColor.YELLOW+"Starting...", true), PLAYING(ChatColor.YELLOW+"In progress", false), RESTARTING(ChatColor.RED+"Restarting", false), BUILDING(ChatColor.AQUA+"Building", true);

private String motd;
private boolean letJoin;
private ServerStage(String motd, boolean letPlayersJoin){
	this.motd = motd;
	this.letJoin = letPlayersJoin;
}

public String getMOTD(){
	return motd;
}
public boolean getAllowJoin(){
	return letJoin;
}
}
