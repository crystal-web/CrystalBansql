package me.devphp.CrystalBansql;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    public static Core plugin;
    public static FileConfiguration configFile;

    public Config(Core plugin)
    {
        this.plugin = plugin;
        makeDefaultConfig();
    }

    public static void load(){
    	
    }
    
    public static void makeDefaultConfig()
    {
        FileConfiguration config = getConfig();

        if(config.get("config") == null)
        {	
            config.set("config.database.host", "localhost");
            config.set("config.database.name", "Ban");
            config.set("config.database.user", "root");
            config.set("config.database.password", "root");

            saveConfig();
        }
        load();
    }

  
    public static void saveConfig()
    {
        try {
            configFile.save(new File(plugin.getDataFolder() + File.separator + "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getConfig() {
        if (configFile == null) {
            configFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));
        }
        return configFile;
    }

	public void prepare() {
		this.plugin.url = "jdbc:mysql://" + getConfig().getString("config.database.host") + ":3306/" + getConfig().getString("config.database.name");
		this.plugin.dbname = getConfig().getString("config.database.name");
		this.plugin.user = getConfig().getString("config.database.user");
		this.plugin.passwd = getConfig().getString("config.database.password");
	}
}

