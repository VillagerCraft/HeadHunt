package nl.villagercraft.paper.storage.yaml;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.BroadcastConfig;
import nl.villagercraft.paper.model.ClickMode;
import nl.villagercraft.paper.model.Find;
import nl.villagercraft.paper.model.Head;
import nl.villagercraft.paper.model.HuntCompletion;
import nl.villagercraft.paper.model.HuntSet;
import nl.villagercraft.paper.model.RewardConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class YamlMapper {

  private YamlMapper() {}

  static ActiveHunt readActiveHunt(ConfigurationSection section) {
    ActiveHunt hunt = new ActiveHunt();
    hunt.setName(section.getString("name"));
    hunt.setActivated(section.getBoolean("activated", true));
    hunt.setPaused(section.getBoolean("paused", false));
    hunt.setTimezone(section.getString("timezone", "server"));
    hunt.setScheduleStart(parseInstant(section.getString("schedule.start")));
    hunt.setScheduleEnd(parseInstant(section.getString("schedule.end")));
    hunt.setClickMode(ClickMode.fromString(section.getString("click-mode")));
    hunt.setRewards(readRewardConfig(section.getConfigurationSection("rewards")));
    hunt.setBroadcast(readBroadcastConfig(section.getConfigurationSection("broadcast")));
    hunt.setSets(readSets(section.getConfigurationSection("sets")));
    hunt.setBanlist(readBanlist(section.getStringList("banlist")));
    return hunt;
  }

  static void writeActiveHunt(FileConfiguration config, ActiveHunt hunt) {
    config.set("hunt.name", hunt.getName());
    config.set("hunt.activated", hunt.isActivated());
    config.set("hunt.paused", hunt.isPaused());
    config.set("hunt.timezone", hunt.getTimezone());
    config.set("hunt.schedule.start", formatInstant(hunt.getScheduleStart()));
    config.set("hunt.schedule.end", formatInstant(hunt.getScheduleEnd()));
    config.set("hunt.click-mode", hunt.getClickMode().name());
    writeRewardConfig(config, "hunt.rewards", hunt.getRewards());
    writeBroadcastConfig(config, "hunt.broadcast", hunt.getBroadcast());
    writeSets(config, "hunt.sets", hunt.getSets());
    config.set(
        "hunt.banlist",
        hunt.getBanlist().stream().map(UUID::toString).sorted().toList());
  }

  static void writeEmptyActiveHunt(FileConfiguration config) {
    config.set("hunt", null);
  }

  static Find readFind(ConfigurationSection section) {
    Find find = new Find();
    find.setHunt(section.getString("hunt"));
    find.setPlayer(parseUuid(section.getString("player")));
    find.setHead(section.getString("head"));
    find.setTimestamp(parseInstant(section.getString("timestamp")));
    return find;
  }

  public static void writeFind(FileConfiguration config, int index, Find find) {
    String path = "finds." + index;
    config.set(path + ".hunt", find.getHunt());
    config.set(path + ".player", find.getPlayer() == null ? null : find.getPlayer().toString());
    config.set(path + ".head", find.getHead());
    config.set(path + ".timestamp", formatInstant(find.getTimestamp()));
  }

  static HuntCompletion readHuntCompletion(ConfigurationSection section) {
    HuntCompletion completion = new HuntCompletion();
    completion.setHunt(section.getString("hunt"));
    completion.setPlayer(parseUuid(section.getString("player")));
    completion.setTimestamp(parseInstant(section.getString("timestamp")));
    return completion;
  }

  public static void writeHuntCompletion(
      FileConfiguration config, int index, HuntCompletion completion) {
    String path = "completions." + index;
    config.set(path + ".hunt", completion.getHunt());
    config.set(
        path + ".player",
        completion.getPlayer() == null ? null : completion.getPlayer().toString());
    config.set(path + ".timestamp", formatInstant(completion.getTimestamp()));
  }

  public static RewardConfig readRewardConfig(ConfigurationSection section) {
    RewardConfig rewards = new RewardConfig();
    if (section == null) {
      return rewards;
    }
    rewards.setHeadDefault(new ArrayList<>(section.getStringList("head.default")));
    rewards.setByHead(readCommandMap(section.getConfigurationSection("head.by-head")));
    rewards.setSetDefault(new ArrayList<>(section.getStringList("set.default")));
    rewards.setBySet(readCommandMap(section.getConfigurationSection("set.by-set")));
    rewards.setHuntCommands(new ArrayList<>(section.getStringList("hunt.commands")));
    return rewards;
  }

  private static void writeRewardConfig(
      FileConfiguration config, String path, RewardConfig rewards) {
    config.set(path + ".head.default", rewards.getHeadDefault());
    config.set(path + ".head.by-head", rewards.getByHead());
    config.set(path + ".set.default", rewards.getSetDefault());
    config.set(path + ".set.by-set", rewards.getBySet());
    config.set(path + ".hunt.commands", rewards.getHuntCommands());
  }

  public static BroadcastConfig readBroadcastConfig(ConfigurationSection section) {
    if (section == null) {
      return new BroadcastConfig();
    }
    return new BroadcastConfig(
        section.getBoolean("set-complete", false),
        section.getBoolean("hunt-complete", true));
  }

  private static void writeBroadcastConfig(
      FileConfiguration config, String path, BroadcastConfig broadcast) {
    config.set(path + ".set-complete", broadcast.isSetComplete());
    config.set(path + ".hunt-complete", broadcast.isHuntComplete());
  }

  private static Map<String, List<String>> readCommandMap(ConfigurationSection section) {
    Map<String, List<String>> map = new LinkedHashMap<>();
    if (section == null) {
      return map;
    }
    for (String key : section.getKeys(false)) {
      map.put(key, new ArrayList<>(section.getStringList(key)));
    }
    return map;
  }

  static Map<String, HuntSet> readSets(ConfigurationSection section) {
    Map<String, HuntSet> sets = new LinkedHashMap<>();
    if (section == null) {
      return sets;
    }
    for (String setName : section.getKeys(false)) {
      ConfigurationSection setSection = section.getConfigurationSection(setName);
      if (setSection == null) {
        continue;
      }
      HuntSet set = new HuntSet(setName);
      ConfigurationSection headsSection = setSection.getConfigurationSection("heads");
      if (headsSection != null) {
        for (String headName : headsSection.getKeys(false)) {
          ConfigurationSection headSection = headsSection.getConfigurationSection(headName);
          if (headSection != null) {
            set.getHeads().put(headName, readHead(headName, setName, headSection));
          }
        }
      }
      sets.put(setName, set);
    }
    return sets;
  }

  private static Head readHead(String headName, String setName, ConfigurationSection section) {
    Head head = new Head();
    head.setName(headName);
    head.setSetName(setName);
    head.setWorld(section.getString("world"));
    head.setX(section.getInt("x"));
    head.setY(section.getInt("y"));
    head.setZ(section.getInt("z"));
    head.setIdentity(section.getString("identity"));
    return head;
  }

  static void writeSets(FileConfiguration config, String path, Map<String, HuntSet> sets) {
    config.createSection(path);
    for (Map.Entry<String, HuntSet> entry : sets.entrySet()) {
      String setPath = path + "." + entry.getKey() + ".heads";
      config.createSection(setPath);
      for (Map.Entry<String, Head> headEntry : entry.getValue().getHeads().entrySet()) {
        Head head = headEntry.getValue();
        String headPath = setPath + "." + headEntry.getKey();
        config.set(headPath + ".world", head.getWorld());
        config.set(headPath + ".x", head.getX());
        config.set(headPath + ".y", head.getY());
        config.set(headPath + ".z", head.getZ());
        config.set(headPath + ".identity", head.getIdentity());
      }
    }
  }

  private static Set<UUID> readBanlist(List<String> values) {
    Set<UUID> banlist = new LinkedHashSet<>();
    for (String value : values) {
      UUID uuid = parseUuid(value);
      if (uuid != null) {
        banlist.add(uuid);
      }
    }
    return banlist;
  }

  private static Instant parseInstant(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(value);
    } catch (DateTimeParseException ignored) {
      return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
    }
  }

  private static String formatInstant(Instant instant) {
    return instant == null ? null : instant.toString();
  }

  private static UUID parseUuid(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }
}
