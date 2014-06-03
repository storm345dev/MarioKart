package net.stormdev.mario.commands;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.server.FullServerManager;
import net.stormdev.mario.server.ServerStage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ForceStartCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		if(!MarioKart.fullServer){
			sender.sendMessage(ChatColor.RED+"FullServer is not enabled!");
			return true;
		}
		ServerStage stage = FullServerManager.get().getStage();
		if(!stage.equals(ServerStage.WAITING)){
			sender.sendMessage(ChatColor.RED+"Unable to start at this time!");
			return true;
		}
		if(FullServerManager.get().voter == null){
			sender.sendMessage(ChatColor.RED+"Unable to start at this time!");
			return true;
		}
		
		FullServerManager.get().voter.closeVotes();
		return true;
	}

}
