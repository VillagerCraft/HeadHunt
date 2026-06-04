package nl.villagercraft.paper.message;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessagesService {

  private final JavaPlugin plugin;
  private final MiniMessage miniMessage = MiniMessage.miniMessage();
  private FileConfiguration messages;

  public MessagesService(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void load() {
    File file = new File(plugin.getDataFolder(), "messages.yml");
    messages = YamlConfiguration.loadConfiguration(file);
  }

  public Component resolve(String key) {
    return resolve(key, Map.of());
  }

  public Component resolve(String key, Map<String, String> placeholders) {
    String raw = messages.getString(key);
    if (raw == null) {
      plugin.getLogger().warning("Missing message key: " + key);
      return Component.text("[" + key + "]");
    }
    String substituted = applyPlaceholders(raw, placeholders);
    try {
      return miniMessage.deserialize(substituted);
    } catch (Exception ex) {
      plugin
          .getLogger()
          .log(Level.WARNING, "Invalid MiniMessage for key " + key + ": " + substituted, ex);
      return Component.text(substituted);
    }
  }

  private static String applyPlaceholders(String raw, Map<String, String> placeholders) {
    String result = raw;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      result = result.replace("{" + entry.getKey() + "}", entry.getValue());
    }
    return result;
  }
}
