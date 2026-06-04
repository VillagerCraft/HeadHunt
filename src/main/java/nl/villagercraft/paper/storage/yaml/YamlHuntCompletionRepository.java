package nl.villagercraft.paper.storage.yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import nl.villagercraft.paper.model.HuntCompletion;
import nl.villagercraft.paper.storage.HuntCompletionRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlHuntCompletionRepository implements HuntCompletionRepository {

  private final JavaPlugin plugin;
  private final File file;
  private final List<HuntCompletion> completions = new ArrayList<>();

  public YamlHuntCompletionRepository(JavaPlugin plugin) {
    this.plugin = plugin;
    this.file = new File(new File(plugin.getDataFolder(), "data"), "hunt-completions.yml");
  }

  @Override
  public List<HuntCompletion> getCompletionsForHunt(String hunt) {
    return completions.stream().filter(c -> hunt.equals(c.getHunt())).toList();
  }

  @Override
  public Optional<HuntCompletion> getCompletion(String hunt, UUID player) {
    for (HuntCompletion completion : completions) {
      if (hunt.equals(completion.getHunt()) && player.equals(completion.getPlayer())) {
        return Optional.of(completion);
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean hasCompletion(String hunt, UUID player) {
    return getCompletion(hunt, player).isPresent();
  }

  @Override
  public void recordCompletion(HuntCompletion completion) {
    if (hasCompletion(completion.getHunt(), completion.getPlayer())) {
      return;
    }
    completions.add(completion);
    save();
  }

  @Override
  public void removeCompletion(String hunt, UUID player) {
    completions.removeIf(
        c -> hunt.equals(c.getHunt()) && player.equals(c.getPlayer()));
    save();
  }

  @Override
  public void removeCompletionsForHunt(String hunt) {
    completions.removeIf(c -> hunt.equals(c.getHunt()));
    save();
  }

  @Override
  public void clear() {
    completions.clear();
    save();
  }

  @Override
  public void load() {
    completions.clear();
    if (!file.exists()) {
      return;
    }
    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    ConfigurationSection section = config.getConfigurationSection("completions");
    if (section == null) {
      return;
    }
    for (String key : section.getKeys(false)) {
      ConfigurationSection completionSection = section.getConfigurationSection(key);
      if (completionSection != null) {
        completions.add(YamlMapper.readHuntCompletion(completionSection));
      }
    }
  }

  @Override
  public void save() {
    FileConfiguration config = new YamlConfiguration();
    config.set("completions", null);
    for (int index = 0; index < completions.size(); index++) {
      YamlMapper.writeHuntCompletion(config, index, completions.get(index));
    }
    try {
      file.getParentFile().mkdirs();
      config.save(file);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, "Failed to save hunt completion data", ex);
    }
  }
}
