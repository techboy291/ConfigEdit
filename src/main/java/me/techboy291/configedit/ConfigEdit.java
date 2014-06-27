package me.techboy291.configedit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

public class ConfigEdit extends JavaPlugin implements Listener {
	@Override
	public void onEnable()
	{
		this.getServer().getPluginManager().registerEvents(this, this);

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			this.getLogger().log(Level.INFO, "Failed to submit plugin metrics.");
		}

		this.getLogger().log(
				Level.INFO,
				"v" + this.getDescription().getVersion()
						+ " is now enabled.");
	}

	@Override
	public void onDisable()
	{
		this.getLogger().log(
				Level.INFO,
				"v" + this.getDescription().getVersion()
						+ " is now disabled.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("edit")
				&& (args.length == 2 || args.length == 3)) {

			File folder;

			if (args[0].startsWith(":")) {
				folder = new File(args[0].substring(1));
			} else {
				Plugin plugin = this.getServer()
						.getPluginManager()
						.getPlugin(args[0]);

				if (plugin == null) {
					sender.sendMessage(ChatColor.RED
							+ "Plugin does not exist.");
					return true;
				}

				folder = plugin.getDataFolder();
			}

			if (!folder.exists()) folder.mkdir();

			File file;

			if (args.length == 2) {
				file = new File(folder, "config.yml");
			} else {
				if (!args[2].endsWith(".yml"))
					args[2] = args[2] + ".yml";

				file = new File(folder, args[2]);
			}

			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					sender.sendMessage(ChatColor.RED
							+ "Configuration file could not be created.");
					return true;
				}
			}

			YamlConfiguration config = YamlConfiguration
					.loadConfiguration(file);

			String[] pair = args[1].split(":");

			if (pair.length != 2) {
				sender.sendMessage(ChatColor.RED
						+ "Invalid key-value pair format: must be 'key:value'.");
				return true;
			}

			Object value;
			String[] values = pair[1].split(",");

			if (values.length > 1) {
				if (values[0].startsWith("+")) {
					values[0] = values[0].substring(1);
					List<String> existing = this
							.getConfigList(pair[0],
									config);

					for (String v : values) {
						existing.add(v);
					}

					value = existing;
				} else if (values[0].startsWith("-")) {
					values[0] = values[0].substring(1);
					List<String> existing = this
							.getConfigList(pair[0],
									config);

					for (String v : values) {
						existing.remove(v);
					}

					value = existing;
				} else {
					value = new ArrayList<>(
							Arrays.asList(values));
				}
			} else if (pair[1].equalsIgnoreCase("null")) {
				value = null;
			} else {
				value = pair[1];
			}

			config.set(pair[0], value);

			try {
				config.save(file);
			} catch (IOException e) {
				sender.sendMessage(ChatColor.RED
						+ "Config file could not be saved.");
				e.printStackTrace();
			}

			sender.sendMessage(ChatColor.GOLD
					+ "Configuration file has been edited successfully!");

			return true;
		}

		return false;
	}

	public List<String> getConfigList(String key, YamlConfiguration config)
	{
		if (config.isList(key))
			return config.getStringList(key);
		else
			return new ArrayList<>();
	}
}
