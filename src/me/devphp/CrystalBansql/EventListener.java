package me.devphp.CrystalBansql;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
	private Core plugin;
	
	public EventListener(Core plugin) {
		this.plugin = plugin;
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
}
