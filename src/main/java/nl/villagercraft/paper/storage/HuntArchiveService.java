package nl.villagercraft.paper.storage;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import nl.villagercraft.paper.model.Find;
import nl.villagercraft.paper.model.HuntCompletion;
import nl.villagercraft.paper.storage.yaml.YamlMapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Persists find and completion data when a hunt is deleted (not queryable in-game). */
public final class HuntArchiveService {

  private final JavaPlugin plugin;
  private final File archiveDir;

  public HuntArchiveService(JavaPlugin plugin) {
    this.plugin = plugin;
    this.archiveDir = new File(new File(plugin.getDataFolder(), "data"), "archives");
  }

  public void archive(String huntName, List<Find> finds, List<HuntCompletion> completions) {
    archiveDir.mkdirs();
    File file = new File(archiveDir, sanitizeFileName(huntName) + ".yml");
    FileConfiguration config = new YamlConfiguration();
    config.set("hunt", huntName);
    config.set("archived-at", Instant.now().toString());
    config.set("finds", null);
    for (int index = 0; index < finds.size(); index++) {
      YamlMapper.writeFind(config, index, finds.get(index));
    }
    config.set("completions", null);
    for (int index = 0; index < completions.size(); index++) {
      YamlMapper.writeHuntCompletion(config, index, completions.get(index));
    }
    try {
      config.save(file);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, "Failed to archive hunt " + huntName, ex);
    }
  }

  private static String sanitizeFileName(String huntName) {
    if (huntName == null || huntName.isBlank()) {
      return "unnamed";
    }
    return huntName.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
