package nl.villagercraft.paper.storage.yaml;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.storage.ActiveHuntRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlActiveHuntRepository implements ActiveHuntRepository {

  private final JavaPlugin plugin;
  private final File file;
  private Optional<ActiveHunt> activeHunt = Optional.empty();

  public YamlActiveHuntRepository(JavaPlugin plugin) {
    this.plugin = plugin;
    this.file = new File(new File(plugin.getDataFolder(), "data"), "active-hunt.yml");
  }

  @Override
  public Optional<ActiveHunt> getActiveHunt() {
    return activeHunt;
  }

  @Override
  public void setActiveHunt(ActiveHunt hunt) {
    this.activeHunt = Optional.of(hunt);
    save();
  }

  @Override
  public void clearActiveHunt() {
    this.activeHunt = Optional.empty();
    save();
  }

  @Override
  public void load() {
    if (!file.exists()) {
      activeHunt = Optional.empty();
      return;
    }
    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    ConfigurationSection huntSection = config.getConfigurationSection("hunt");
    if (huntSection == null) {
      activeHunt = Optional.empty();
      return;
    }
    ActiveHunt hunt = YamlMapper.readActiveHunt(huntSection);
    if (!huntSection.contains("activated") && config.getBoolean("active", false)) {
      hunt.setActivated(true);
    }
    activeHunt = Optional.of(hunt);
  }

  @Override
  public void save() {
    FileConfiguration config = new YamlConfiguration();
    if (activeHunt.isPresent()) {
      YamlMapper.writeActiveHunt(config, activeHunt.get());
    } else {
      YamlMapper.writeEmptyActiveHunt(config);
    }
    try {
      file.getParentFile().mkdirs();
      config.save(file);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, "Failed to save active hunt data", ex);
    }
  }
}
