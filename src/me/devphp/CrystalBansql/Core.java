package me.devphp.CrystalBansql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import me.devphp.CrystalBansql.Commands.CommandBan;
import me.devphp.CrystalBansql.Commands.CommandExportban;
import me.devphp.CrystalBansql.Commands.CommandImportban;
import me.devphp.CrystalBansql.Commands.CommandPardon;
import me.devphp.CrystalBansql.Commands.CommandTempban;

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

public class Core extends JavaPlugin {
	public String prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + "Bansql"
			+ ChatColor.GRAY + "] " + ChatColor.RESET;
	public String url;
	public String dbname;
	public String user;
	public String passwd;

	public Connection connection;
	public Statement state;

	public void onEnable() {
		this.getLogger().info("Chargement de la configuration...");
		Config conf = new Config(this);
		conf.prepare();

		// getCommand("ban").setExecutor(new Ban(this));
		// getCommand("pardon").setExecutor(new Pardon(this));
		this.getLogger().info("Initialisation des events...");
		this.getServer().getPluginManager()
				.registerEvents(new EventListener(this), this);
		
		this.getCommand("ban").setExecutor(new CommandBan(this));
		this.getCommand("tempban").setExecutor(new CommandTempban(this));
		this.getCommand("pardon").setExecutor(new CommandPardon(this));
		
		this.getCommand("exportban").setExecutor(new CommandExportban(this));
		this.getCommand("importban").setExecutor(new CommandImportban(this));
		
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
			this.connection = DriverManager.getConnection(this.url, this.user,
					this.passwd);
			this.state = this.connection.createStatement();
			java.util.Date currentDate = new java.util.Date();
			long currenttimeInMillis = currentDate.getTime();

			// this.state.execute("USE " + this.dbname);
			this.state
					.execute("CREATE TABLE IF NOT EXISTS `bansql` ("
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
			this.connection = DriverManager.getConnection(this.url, this.user,
					this.passwd);
			this.state = this.connection.createStatement();
			java.util.Date currentDate = new java.util.Date();
			long currenttimeInMillis = currentDate.getTime();
			this.state.execute("DELETE FROM `bansql` WHERE `banto` < "
					+ (currenttimeInMillis / 1000) + " AND `banto` > 0;");
			// this.getLogger().severe("DELETE FROM `bansql` WHERE `banto` < "+(currenttimeInMillis/1000)+" AND `banto` > 0;");
			for (Player player : Bukkit.getOnlinePlayers()) {
				try {

					String query = "SELECT reason FROM `"
							+ this.dbname
							+ "`.`bansql` WHERE `uid` LIKE '"
							+ player.getUniqueId().toString()
							+ "' OR `ip` LIKE '"
							+ player.getAddress().getAddress().getHostAddress()
									.toString() + "'";
					// this.getLogger().severe(query);
					ResultSet result = this.state.executeQuery(query);
					Thread.sleep(100L);
					while (result.next()) {
						player.kickPlayer(result.getString("reason"));
					}// */

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

	public boolean pardon(String playerToUnban, CommandSender sender) {
		try {
			this.connection = DriverManager.getConnection(
					this.url, this.user, this.passwd);
			this.state = this.connection.createStatement();
			this.getLogger().info(
					this.prefix + ChatColor.RED + "Connection success");

			String query = "SELECT id FROM `bansql` WHERE `pseudo` LIKE '"
					+ playerToUnban + "'";// */
			ResultSet result = this.state.executeQuery(query);
			try {
				while (result.next()) {
					if (this.state.isClosed()) {
						this.state = this.connection
								.createStatement();
					}
					this.state
							.executeUpdate("DELETE FROM bansql WHERE id = "
									+ result.getInt("id"));
					return true;
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				this.getLogger().severe(e.getMessage());
			}
			// */
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			this.getLogger().severe(e1.getMessage());
		}
		return false;
	}

	public boolean tempban(Player player, CommandSender sender, String reason, int time) {
		try {
			this.connection = DriverManager.getConnection(
					this.url, this.user, this.passwd);
			this.state = this.connection.createStatement();

			java.util.Date currentDate = new java.util.Date();
			long currenttimeInMillis = currentDate.getTime();

			String query = "INSERT INTO `bansql` (uid, ip, pseudo, reason, admin, bantime, banto) VALUES ("
					+ "'" + player.getUniqueId().toString() + "', "
					+ "'" + player.getAddress().getAddress().getHostAddress() + "', "
					+ "'" + player.getPlayer().getName().toString() + "',"
					+ "'" + reason + "',"
					+ "'" + sender.getName().toString()
					+ "'," + currenttimeInMillis / 1000 + "," + time + ");";
			int insert = this.state.executeUpdate(query);
			if (insert == 1) {
				if (player.isOnline()) {
					player.kickPlayer(reason);
				}
				return true;
			}// */
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.getLogger().severe(e.getMessage());
		}
		return false;
	}

	public boolean ban(Player player, CommandSender sender, String reason) {
		try {
			this.connection = DriverManager.getConnection(
					this.url, this.user, this.passwd);
			this.state = this.connection.createStatement();

			java.util.Date currentDate = new java.util.Date();
			long currenttimeInMillis = currentDate.getTime();

			String query = "INSERT INTO `bansql` (uid, ip, pseudo, reason, admin, bantime, banto) VALUES ("
					+ "'" + player.getUniqueId().toString() + "', "
					+ "'" + player.getAddress().getAddress().getHostAddress() + "', "
					+ "'" + player.getPlayer().getName().toString() + "',"
					+ "'" + reason + "',"
					+ "'" + sender.getName().toString()
					+ "'," + currenttimeInMillis / 1000 + "," + "0" + ");";
			int insert = this.state.executeUpdate(query);
			if (insert == 1) {
				if (player.isOnline()) {
					player.kickPlayer(reason);
				}
				return true;
			}// */
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.getLogger().severe(e.getMessage());
		}
		return false;
	}

	public static String implodeArray(String[] inputArray, String glueString) {
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
