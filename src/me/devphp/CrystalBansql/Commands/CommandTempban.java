package me.devphp.CrystalBansql.Commands;

import me.devphp.CrystalBansql.Core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTempban implements CommandExecutor {
	private Core plugin;
	public CommandTempban(Core core) {
		this.plugin = core;
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		
		if (sender instanceof Player) {
			if (!sender.isOp() && !sender.hasPermission("bansql.ban")) {
				sender.sendMessage(this.plugin.prefix + ChatColor.RED + "Permission refusé");
				return false;
			}
		}
		if (args.length < 4) {
			sender.sendMessage(this.plugin.prefix + "usage: /tempban {player} {number} {minute|day|week|month|year} {reason}");
			return false;
		}
		
		String playerToBan = args[1];
		int time = Integer.parseInt(args[2]);
		String period = args[3];
		
		args[0] = "";
		args[1] = "";
		args[2] = "";
		args[3] = "";
		String reason = Core.implodeArray(args, " ").replace("\"", "&quot;")
				.replace("'", "&#039;").trim();
		
		if (period.equalsIgnoreCase("minute")) {
			time = time * 60;
		}else if (period.equalsIgnoreCase("hour")) {
			time = time * 3600;
		}else if (period.equalsIgnoreCase("day")) {
			time = time * 86400;
		} else if (period.equalsIgnoreCase("week")) {
			time = time * 604800;
		} else if (period.equalsIgnoreCase("month")) {
			time = time * 2592000;
		} else if (period.equalsIgnoreCase("month")) {
			time = time * 31536000;
		} else {
			sender.sendMessage(this.plugin.prefix + "usage: /tempban {player} {number} {minute|day|week|month|year} {reason}");
			return false;
		}
		
		java.util.Date currentDate = new java.util.Date();
        long currenttimeInMillis = currentDate.getTime();
        time = (int) ((currenttimeInMillis/1000) + time);
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().toString().equalsIgnoreCase(playerToBan)) {
				sender.sendMessage(this.plugin.prefix + "Joueur bannis temporairement");
				if (this.plugin.tempban(player, sender, reason, time)) {
					sender.sendMessage(this.plugin.prefix + "Joueur bannis");
				} else {
					sender.sendMessage(this.plugin.prefix + "Joueur introuvable");
				}
				return true;
			}
		}
		
		return false;
	}
}
