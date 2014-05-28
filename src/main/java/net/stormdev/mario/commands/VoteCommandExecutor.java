package net.stormdev.mario.commands;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.server.FullServerManager;
import net.stormdev.mario.server.ServerStage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		if(!MarioKart.fullServer){
			sender.sendMessage(ChatColor.RED+"MarioKart full server mode disabled, so no voting!");
			return true;
		}
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED+"Players only!");
			return true;
		}
		if(args.length < 1){
			return false;
		}
		if(!FullServerManager.get().getStage().equals(ServerStage.WAITING)){
			sender.sendMessage(ChatColor.RED+"You may not vote at this time!");
			return true;
		}
		StringBuilder track = new StringBuilder(args[0]);
		for(int i=1;i<args.length;i++){
			track.append(" ").append(args[i]);
		}
		FullServerManager.get().voter.castVote((Player)sender, track.toString());
		return true;
	}

}
