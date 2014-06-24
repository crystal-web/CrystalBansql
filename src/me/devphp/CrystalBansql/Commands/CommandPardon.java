package me.devphp.CrystalBansql.Commands;

import me.devphp.CrystalBansql.Core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPardon implements CommandExecutor {
	private Core plugin;
	
	public CommandPardon(Core core) {
		this.plugin = core;
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (sender instanceof Player) {
			if (!sender.isOp() && !sender.hasPermission("bansql.pardon")) {
				sender.sendMessage(this.plugin.prefix + ChatColor.RED + "Permission refusé");;
				return false;
			}
		}

		if (args.length < 1) {
			sender.sendMessage("usage: /pardon {player}");
			return false;
		}
		String playerToUnban = args[1];
		
		if (this.plugin.pardon(playerToUnban, sender)) {
			sender.sendMessage(this.plugin.prefix + "Joueur pardonner");
		} else {
			sender.sendMessage(this.plugin.prefix + "Joueur introuvable");
		}
		return false;
	}
}
