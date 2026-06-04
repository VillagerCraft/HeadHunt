package nl.villagercraft.paper.storage.yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import nl.villagercraft.paper.model.Find;
import nl.villagercraft.paper.storage.FindRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlFindRepository implements FindRepository {

  private final JavaPlugin plugin;
  private final File file;
  private final List<Find> finds = new ArrayList<>();

  public YamlFindRepository(JavaPlugin plugin) {
    this.plugin = plugin;
    this.file = new File(new File(plugin.getDataFolder(), "data"), "finds.yml");
  }

  @Override
  public List<Find> getFinds() {
    return List.copyOf(finds);
  }

  @Override
  public List<Find> getFindsForHunt(String hunt) {
    return finds.stream().filter(find -> hunt.equals(find.getHunt())).toList();
  }

  @Override
  public List<Find> getFindsForHead(String headName) {
    return finds.stream().filter(find -> headName.equals(find.getHead())).toList();
  }

  @Override
  public int countDistinctPlayersForHead(String hunt, String headName) {
    Set<UUID> players = new HashSet<>();
    for (Find find : finds) {
      if (hunt.equals(find.getHunt()) && headName.equals(find.getHead()) && find.getPlayer() != null) {
        players.add(find.getPlayer());
      }
    }
    return players.size();
  }

  @Override
  public int countFindsForPlayer(String hunt, UUID player) {
    int count = 0;
    for (Find find : finds) {
      if (hunt.equals(find.getHunt()) && player.equals(find.getPlayer())) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int countFindsForPlayerInSet(
      String hunt, UUID player, Collection<String> headNames) {
    if (headNames.isEmpty()) {
      return 0;
    }
    Set<String> heads = headNames instanceof Set ? (Set<String>) headNames : new HashSet<>(headNames);
    int count = 0;
    for (Find find : finds) {
      if (hunt.equals(find.getHunt())
          && player.equals(find.getPlayer())
          && heads.contains(find.getHead())) {
        count++;
      }
    }
    return count;
  }

  @Override
  public boolean hasFind(String hunt, UUID player, String head) {
    for (Find find : finds) {
      if (hunt.equals(find.getHunt())
          && player.equals(find.getPlayer())
          && head.equals(find.getHead())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addFind(Find find) {
    finds.add(find);
    save();
  }

  @Override
  public void removeFindsForHead(String headName) {
    finds.removeIf(find -> headName.equals(find.getHead()));
    save();
  }

  @Override
  public void removeFindsForHunt(String hunt) {
    finds.removeIf(find -> hunt.equals(find.getHunt()));
    save();
  }

  @Override
  public void removeFindsForPlayer(String hunt, UUID player) {
    finds.removeIf(find -> hunt.equals(find.getHunt()) && player.equals(find.getPlayer()));
    save();
  }

  @Override
  public void removeFindsForPlayerInSet(
      String hunt, UUID player, Collection<String> headNames) {
    if (headNames.isEmpty()) {
      return;
    }
    Set<String> heads = headNames instanceof Set ? (Set<String>) headNames : new HashSet<>(headNames);
    finds.removeIf(
        find ->
            hunt.equals(find.getHunt())
                && player.equals(find.getPlayer())
                && heads.contains(find.getHead()));
    save();
  }

  @Override
  public void removeFindForPlayer(String hunt, UUID player, String headName) {
    finds.removeIf(
        find ->
            hunt.equals(find.getHunt())
                && player.equals(find.getPlayer())
                && headName.equals(find.getHead()));
    save();
  }

  @Override
  public void clear() {
    finds.clear();
    save();
  }

  @Override
  public void load() {
    finds.clear();
    if (!file.exists()) {
      return;
    }
    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    ConfigurationSection findsSection = config.getConfigurationSection("finds");
    if (findsSection == null) {
      return;
    }
    for (String key : findsSection.getKeys(false)) {
      ConfigurationSection findSection = findsSection.getConfigurationSection(key);
      if (findSection != null) {
        finds.add(YamlMapper.readFind(findSection));
      }
    }
  }

  @Override
  public void save() {
    FileConfiguration config = new YamlConfiguration();
    config.set("finds", null);
    for (int index = 0; index < finds.size(); index++) {
      YamlMapper.writeFind(config, index, finds.get(index));
    }
    try {
      file.getParentFile().mkdirs();
      config.save(file);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, "Failed to save find data", ex);
    }
  }
}
