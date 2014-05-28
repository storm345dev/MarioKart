package net.stormdev.mario.server;

public enum ServerStage {
WAITING("waiting"), PLAYING("in progress"), RESTARTING("restarting");

private String motd;
private ServerStage(String motd){
	this.motd = motd;
}

public String getMOTD(){
	return motd;
}
}
