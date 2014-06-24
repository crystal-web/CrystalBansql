package me.devphp.CrystalBansql.Commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.devphp.CrystalBansql.Core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExportban implements CommandExecutor {
	private Core plugin;
	
	public CommandExportban(Core core) {
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
		
		try {
			this.plugin.connection = DriverManager.getConnection(this.plugin.url, this.plugin.user, this.plugin.passwd);
			this.plugin.state = this.plugin.connection.createStatement();
			
			String query = "SELECT uid, ip, pseudo, admin, reason, bantime, banto FROM `" + this.plugin.dbname + "`.`bansql` LIMIT 1000";
//			this.getLogger().severe(query);
			ResultSet result = this.plugin.state.executeQuery(query);
			
			//création ou ajout dans le fichier texte
			try {
				String fichier = this.plugin.getDataFolder() + File.separator + "ban.txt";
				FileWriter fw = new FileWriter (fichier);
				BufferedWriter bw = new BufferedWriter (fw);
				PrintWriter fichierSortie = new PrintWriter (bw); 
				
				int exporte = 0;
				while (result.next()) {
					exporte++;
					fichierSortie.println (result.getString("uid") + ";" + result.getString("ip") + ";" + result.getString("pseudo") + ";" + result.getString("admin") + ";" + result.getString("reason") + ";" + result.getString("bantime") + ";" + result.getString("banto"));	
				}
				fichierSortie.close();
				sender.sendMessage(this.plugin.prefix + "Le fichier a été créé et " + exporte + " ont été enregistré!");
				return true;
			}
			catch (Exception e){
				this.plugin.getLogger().severe(e.getMessage());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.plugin.getLogger().severe(e.getMessage());
		}
		return false;
	}

}
