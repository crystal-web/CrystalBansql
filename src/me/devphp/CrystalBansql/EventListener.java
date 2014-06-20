package me.devphp.CrystalBansql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
	private Core plugin;
	
	public EventListener(Core plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player sender = event.getPlayer();
		String message = event.getMessage();
		String[] args = message.split(" ");
		
		if (args[0].equalsIgnoreCase("/exportban")) {
			try {
				this.plugin.connection = DriverManager.getConnection(this.plugin.url, this.plugin.user, this.plugin.passwd);
				this.plugin.state = this.plugin.connection.createStatement();
				
				String query = "SELECT uid, ip, pseudo, admin, reason, bantime, banto FROM `" + this.plugin.dbname + "`.`bansql` LIMIT 1000";
//				this.getLogger().severe(query);
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
				}
				catch (Exception e){
					System.out.println(e.toString());
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				this.plugin.getLogger().severe(e.getMessage());
			}
			

		}else if (args[0].equalsIgnoreCase("/importban")) {
			
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
					String query = "SELECT reason FROM `" + this.plugin.dbname + "`.`bansql` WHERE `uid` LIKE '"
							+ uid
							+ "' OR `ip` LIKE '"
							+ ip
							+ "'";
//					this.getLogger().severe(query);
					ResultSet result = this.plugin.state.executeQuery(query);
					if (result.getRow() == 0) {
						enregistrement++;
						this.plugin.state.execute("INSERT INTO `" + this.plugin.dbname + "`.`bansql` (`id`, `uid`, `ip`, `pseudo`, `admin`, `reason`, `bantime`, `banto`) VALUES (NULL, '" + uid + "', '" + ip + "', '" + pseudo + "', '" + admin + "', '" + reason + "', '" + bantime + "', '" + banto + "');");
					}//*/
				}
				br.close();
				
				sender.sendMessage(this.plugin.prefix + " Importation effectué " + entree + " dans le fichier et " + enregistrement + " enregistré en base de donnée");
			}		
			catch (Exception e){
				System.out.println(e.toString());
			}
		} else if (args[0].equalsIgnoreCase("/tempban")) {
			if (!sender.isOp() && !sender.hasPermission("bansql.ban")) {
				sender.sendMessage(this.plugin.prefix + ChatColor.RED + "Permission refusé");
				return;
			}

			if (args.length < 5) {
				sender.sendMessage(this.plugin.prefix + "usage: /tempban {player} {number} {minute|day|week|month|year} {reason}");
				return;
			}
			
			String playerToBan = args[1];
			int time = Integer.parseInt(args[2]);
			String period = args[3];
			
			args[0] = "";
			args[1] = "";
			args[2] = "";
			args[3] = "";
			String reason = implodeArray(args, " ").replace("\"", "&quot;")
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
				return;
			}
			
			java.util.Date currentDate = new java.util.Date();
	        long currenttimeInMillis = currentDate.getTime();
	        
	        time = (int) ((currenttimeInMillis/1000) + time);
			
			
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toString().equalsIgnoreCase(playerToBan)) {
					sender.sendMessage(this.plugin.prefix + "Joueur bannis temporairement");
					this.tempban(player, sender, reason, time);
					return;
				}
			}
			
			/*for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
				if (player.getName().toString().equalsIgnoreCase(playerToBan)) {
					sender.sendMessage(this.prefix + "Joueur bannis temporairement");
					this.tempban(player.getPlayer(), sender, reason, time);
					return;
				}
			}//*/
			
		}else if (args[0].equalsIgnoreCase("/ban")) {
			if (!sender.isOp() && !sender.hasPermission("bansql.ban")) {
				sender.sendMessage(this.plugin.prefix + ChatColor.RED + "Permission refusé");
				return;
			}

			if (args.length < 3) {
				sender.sendMessage(this.plugin.prefix + "usage: /ban {player} {reason}");
				return;
			}

			String playerToBan = args[1];
			args[0] = "";
			args[1] = "";
			String reason = implodeArray(args, " ").replace("\"", "&quot;")
					.replace("'", "&#039;").trim();

			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toString().equalsIgnoreCase(playerToBan)) {
					sender.sendMessage(this.plugin.prefix + "Joueur bannis");
					this.ban(player, sender, reason);
					return;
				}
			}
			
			/*for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
				if (player.getName().toString().equalsIgnoreCase(playerToBan)) {
					sender.sendMessage(this.prefix + "Joueur bannis");
					this.ban(player.getPlayer(), sender, reason);
					return;
				}
			}//*/
			return;
			
		} else if (args[0].equalsIgnoreCase("/pardon")) {
			
			if (!sender.isOp() && !sender.hasPermission("bansql.pardon")) {
				sender.sendMessage(this.plugin.prefix + ChatColor.RED + "Permission refusé");
				return;
			}

			if (args.length < 2) {
				sender.sendMessage("usage: /pardon {player}");
				return;
			}
			String playerToUnban = args[1];
			
			this.pardon(playerToUnban, sender);
		}

		return;
	}
	
	

	
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		try {
			this.plugin.connection = DriverManager.getConnection(this.plugin.url, this.plugin.user, this.plugin.passwd);
			this.plugin.state = this.plugin.connection.createStatement();
			
			String query = "SELECT reason FROM `bansql` WHERE `uid` LIKE '"
					+ player.getUniqueId().toString()
					+ "' OR `ip` LIKE '"
					+ player.getAddress().getAddress().getHostAddress()
							.toString() + "'";
			ResultSet result = this.plugin.state.executeQuery(query);			
			
			// Petit delay pour le kick
			Thread.sleep(50L);
				while(result.next()) {
					player.kickPlayer(result.getString("reason"));
				return;
				}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	

	

	public void pardon(String playerToUnban, Player sender){
		try {
			this.plugin.connection = DriverManager.getConnection(this.plugin.url, this.plugin.user, this.plugin.passwd);
			this.plugin.state = this.plugin.connection.createStatement();
			this.plugin.getLogger().info(this.plugin.prefix + ChatColor.RED + "Connection success");
			
			
			String query = "SELECT id FROM `bansql` WHERE `pseudo` LIKE '"
					+ playerToUnban + "'";//*/
			ResultSet result = this.plugin.state.executeQuery(query);
			try {
				while(result.next()) {
					if (this.plugin.state.isClosed()) {
						this.plugin.state = this.plugin.connection.createStatement();
					}
					this.plugin.state
					.executeUpdate("DELETE FROM bansql WHERE id = "
							+ result.getInt("id"));
				}
				sender.sendMessage("Joueur débannis");
	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				this.plugin.getLogger().severe(e.getMessage());
			}
			//*/
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			this.plugin.getLogger().severe(e1.getMessage());
		}
	}
	

	public void tempban(Player player, Player sender, String reason, int time){
		try {
			this.plugin.connection = DriverManager.getConnection(this.plugin.url, this.plugin.user, this.plugin.passwd);
			this.plugin.state = this.plugin.connection.createStatement();

			java.util.Date currentDate = new java.util.Date();
	        long currenttimeInMillis = currentDate.getTime();
	 
	        
	        
			String query = "INSERT INTO `bansql` (uid, ip, pseudo, reason, admin, bantime, banto) VALUES ("
					+ "'" + player.getUniqueId().toString() + "', "
					+ "'" + player.getAddress().getAddress().getHostAddress() + "', "
					+ "'" + player.getPlayer().getName().toString() + "',"
					+ "'" + reason + "',"
					+ "'" + sender.getName().toString() + "',"
					+ currenttimeInMillis/1000 + ","
					+ time
					+ ");";
			int insert = this.plugin.state.executeUpdate(query);
			if (insert == 1) {
				if (player.isOnline()) {
					player.kickPlayer(reason);
				}
				sender.sendMessage("Joueur bannis");
			}//*/
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.plugin.getLogger().severe(e.getMessage());
		}
	}
	
	public void ban(Player player, Player sender, String reason){
		try {
			this.plugin.connection = DriverManager.getConnection(this.plugin.url, this.plugin.user, this.plugin.passwd);
			this.plugin.state = this.plugin.connection.createStatement();

			java.util.Date currentDate = new java.util.Date();
	        long currenttimeInMillis = currentDate.getTime();
	 
	        
	        
			String query = "INSERT INTO `bansql` (uid, ip, pseudo, reason, admin, bantime, banto) VALUES ("
					+ "'" + player.getUniqueId().toString() + "', "
					+ "'" + player.getAddress().getAddress().getHostAddress() + "', "
					+ "'" + player.getPlayer().getName().toString() + "',"
					+ "'" + reason + "',"
					+ "'" + sender.getName().toString() + "',"
					+ currenttimeInMillis/1000 + ","
					+ "0"
					+ ");";
			int insert = this.plugin.state.executeUpdate(query);
			if (insert == 1) {
				if (player.isOnline()) {
					player.kickPlayer(reason);
				}
				sender.sendMessage("Joueur bannis");
			}//*/
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.plugin.getLogger().severe(e.getMessage());
		}
	}
	
	
	public static String implodeArray(String[] inputArray, String glueString)
	  {
	    String output = "";

	    if (inputArray.length > 0) {
	      StringBuilder sb = new StringBuilder();
	      sb.append(inputArray[0]);

	      for (int i = 1; i < inputArray.length; i++) {
	        sb.append(glueString);
	        sb.append(inputArray[i]);
	      }

	      output = sb.toString();
	    }

	    return output;
	  }
}
