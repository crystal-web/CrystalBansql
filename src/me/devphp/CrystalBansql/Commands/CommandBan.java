package me.devphp.CrystalBansql.Commands;

import me.devphp.CrystalBansql.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandBan implements CommandExecutor {
	private Core plugin;
	
	public CommandBan(Core core) {
		this.plugin = core;
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		
		if (sender instanceof Player) {
			if (!sender.isOp() && !sender.hasPermission("bansql.ban")) {
				sender.sendMessage(this.plugin.prefix + ChatColor.RED + "Permission refusé");
				return true;
			}
		}
		
		if (args.length < 2) {
			sender.sendMessage(this.plugin.prefix + "usage: /ban {player} {reason}");
			return true;
		}

		String playerToBan = args[1];
		args[0] = "";
		args[1] = "";
		String reason = Core.implodeArray(args, " ").replace("\"", "&quot;")
				.replace("'", "&#039;").trim();

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().toString().equalsIgnoreCase(playerToBan)) {
				sender.sendMessage(this.plugin.prefix + "Joueur bannis");
				if (this.plugin.ban(player, sender, reason)) {
					sender.sendMessage(this.plugin.prefix + "Joueur bannis");
				} else {
					sender.sendMessage(this.plugin.prefix + "Joueur introuvable");
				}
				return true;
			}
		}
		
		sender.sendMessage(this.plugin.prefix + " Joueur introuvable");
		return false;
	}
}
