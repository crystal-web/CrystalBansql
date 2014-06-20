package me.devphp.CrystalBansql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Core extends JavaPlugin implements Listener {
	public String prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + "Bansql" + ChatColor.GRAY + "] " + ChatColor.RESET;
	public String url;
	public String dbname;
	public String user;
	public String passwd;

	Connection connection;
	Statement state;
	

	public void onEnable() {
		this.getLogger().info("Chargement de la configuration...");
		Config conf = new Config(this);
		conf.prepare();

		
		//getCommand("ban").setExecutor(new Ban(this));
		//getCommand("pardon").setExecutor(new Pardon(this));
		this.getLogger().info("Initialisation des events...");
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// Thread 
		this.getLogger().info("Initialisation du thread...");
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
			// THREAD 100 = 5sec
				thread();
			}
		}, 0L, 1200L);
		
		connect();
	}
	

	public void connect() {
		this.getLogger().info("Connection à la base de donnée...");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			this.getLogger().severe("Class  not found: com.mysql.jdbc.Driver");
		}

		try {
			this.connection = DriverManager.getConnection(this.url, this.user, this.passwd);
			this.state = this.connection.createStatement();
			java.util.Date currentDate = new java.util.Date();
	        long currenttimeInMillis = currentDate.getTime();
	        
	        //this.state.execute("USE " + this.dbname);
			this.state.execute("CREATE TABLE IF NOT EXISTS `bansql` ("
					  + "`id` int(11) NOT NULL AUTO_INCREMENT,"
					  + "`uid` varchar(255) NOT NULL,"
					  + "`ip` varchar(100) NOT NULL,"
					  + "`pseudo` varchar(16) NOT NULL,"
					  + "`admin` varchar(16) NOT NULL,"
					  + "`reason` varchar(255) NOT NULL,"
					  + "`bantime` int(11) NOT NULL,"
					  + "`banto` int(11) NOT NULL,"
					  + "UNIQUE KEY `id` (`id`)"
					  + ") ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;");
		} catch (SQLException e) {
			this.getLogger().severe(e.getMessage());
			this.getLogger().severe(this.url + " " + this.user + " *******");
			this.getServer().getPluginManager().disablePlugins();
		}
	}
	
	public void thread() {
		try {
			this.connection = DriverManager.getConnection(this.url, this.user, this.passwd);
			this.state = this.connection.createStatement();
			java.util.Date currentDate = new java.util.Date();
	        long currenttimeInMillis = currentDate.getTime();
			this.state.execute("DELETE FROM `bansql` WHERE `banto` < "+(currenttimeInMillis/1000)+" AND `banto` > 0;");
//			this.getLogger().severe("DELETE FROM `bansql` WHERE `banto` < "+(currenttimeInMillis/1000)+" AND `banto` > 0;");
			for (Player player : Bukkit.getOnlinePlayers()) {
				try {

					String query = "SELECT * FROM `" + this.dbname + "`.`bansql` WHERE `uid` LIKE '"
							+ player.getUniqueId().toString()
							+ "' OR `ip` LIKE '"
							+ player.getAddress().getAddress().getHostAddress().toString() + "'";
//					this.getLogger().severe(query);
					ResultSet result = this.state.executeQuery(query);
					Thread.sleep(100L);
					while (result.next()) {
						player.kickPlayer(result.getString("reason"));
					}//*/

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					this.getLogger().severe(e.getMessage());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					this.getLogger().severe(e.getMessage());
				}
			}

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			this.getLogger().severe(e1.getMessage());
		}
	}
}


