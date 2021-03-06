package de.myzelyam.supervanish.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.myzelyam.supervanish.SuperVanish;

public class ConfigCfg {

	public SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
			.getPlugin("SuperVanish");

	private final String configFile;

	private File config;

	private FileConfiguration fileConfiguration;

	public ConfigCfg() {
		this.configFile = "config.yml";
		File dataFolder = plugin.getDataFolder();
		if (dataFolder == null)
			throw new IllegalStateException();
		this.config = new File(plugin.getDataFolder(), configFile);
	}

	public void reloadConfig() {
		fileConfiguration = YamlConfiguration.loadConfiguration(config);
		InputStream defConfigStream = plugin.getResource(configFile);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(new InputStreamReader(defConfigStream));
			fileConfiguration.setDefaults(defConfig);
		}
	}

	public FileConfiguration getConfig() {
		if (fileConfiguration == null) {
			this.reloadConfig();
		}
		return fileConfiguration;
	}

	public void saveDefaultConfig() {
		if (!config.exists()) {
			this.plugin.saveResource(configFile, false);
		}
	}
}
