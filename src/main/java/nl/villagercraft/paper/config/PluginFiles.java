package nl.villagercraft.paper.config;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginFiles {

  private PluginFiles() {}

  public static void ensureDefaults(JavaPlugin plugin) {
    plugin.getDataFolder().mkdirs();
    plugin.saveDefaultConfig();

    File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
    if (!messagesFile.exists()) {
      plugin.saveResource("messages.yml", false);
    }

    new File(plugin.getDataFolder(), "data").mkdirs();
  }
}
