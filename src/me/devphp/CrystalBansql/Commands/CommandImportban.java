package me.devphp.CrystalBansql.Commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.devphp.CrystalBansql.Core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandImportban implements CommandExecutor {
	private Core plugin;
	
	public CommandImportban(Core core) {
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.plugin.getLogger().severe(e.getMessage());
		}
		
		
		int entree = 0;
		int enregistrement = 0;
		String chaine="";
		String fichier = this.plugin.getDataFolder() + File.separator + "ban.txt";
		//lecture du fichier texte	
		try{
			InputStream ips=new FileInputStream(fichier); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				entree++;
				String[] data = ligne.split(";");
				String uid = data[0];
				String ip = data[1];
				String pseudo = data[2];
				String admin = data[3];
				String reason = data[4];
				String bantime = data[5];
				String banto = data[6];
				
				// Recherche dans la DB si il existe
				String query = "SELECT reason FROM `" + this.plugin.dbname + "`.`bansql` WHERE `uid` LIKE '" + uid + "' OR `ip` LIKE '" + ip + "'";
//				this.getLogger().severe(query);
				ResultSet result = this.plugin.state.executeQuery(query);
				if (result.getRow() == 0) {
					enregistrement++;
					this.plugin.state.execute("INSERT INTO `" + this.plugin.dbname + "`.`bansql` (`id`, `uid`, `ip`, `pseudo`, `admin`, `reason`, `bantime`, `banto`) VALUES (NULL, '" + uid + "', '" + ip + "', '" + pseudo + "', '" + admin + "', '" + reason + "', '" + bantime + "', '" + banto + "');");
				}//*/
			}
			br.close();
			
			sender.sendMessage(this.plugin.prefix + " Importation effectué " + entree + " dans le fichier et " + enregistrement + " enregistré en base de donnée");
			return true;
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
		return false;
	}

}
