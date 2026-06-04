package nl.villagercraft.paper.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.BroadcastConfig;
import nl.villagercraft.paper.model.ClickMode;
import nl.villagercraft.paper.model.RewardConfig;
import nl.villagercraft.paper.storage.yaml.YamlMapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class HuntTemplateLoader {

  private static final String SERVER_TIMEZONE = "server";

  private HuntTemplateLoader() {}

  public static ActiveHunt loadSnapshot(FileConfiguration config) throws InvalidHuntConfigException {
    ConfigurationSection huntSection = config.getConfigurationSection("hunt");
    if (huntSection == null) {
      throw new InvalidHuntConfigException("Missing hunt section in config.yml");
    }

    String name = huntSection.getString("name");
    if (name == null || name.isBlank()) {
      throw new InvalidHuntConfigException("hunt.name is missing or empty");
    }

    String timezone = huntSection.getString("timezone", SERVER_TIMEZONE);
    if (!SERVER_TIMEZONE.equalsIgnoreCase(timezone.trim())) {
      throw new InvalidHuntConfigException(
          "Unsupported hunt.timezone: " + timezone + " (v1 supports only \"server\")");
    }

    ClickMode clickMode = parseClickMode(config.getString("click-mode"));
    Instant scheduleStart = parseSchedule(huntSection.getString("schedule.start"));
    Instant scheduleEnd = parseSchedule(huntSection.getString("schedule.end"));

    RewardConfig rewards = YamlMapper.readRewardConfig(config.getConfigurationSection("rewards"));
    BroadcastConfig broadcast =
        YamlMapper.readBroadcastConfig(config.getConfigurationSection("broadcast"));

    ActiveHunt hunt = new ActiveHunt();
    hunt.setName(name.trim());
    hunt.setPaused(false);
    hunt.setTimezone(SERVER_TIMEZONE);
    hunt.setScheduleStart(scheduleStart);
    hunt.setScheduleEnd(scheduleEnd);
    hunt.setClickMode(clickMode);
    hunt.setRewards(rewards);
    hunt.setBroadcast(broadcast);
    return hunt;
  }

  private static ClickMode parseClickMode(String value) throws InvalidHuntConfigException {
    if (value == null || value.isBlank()) {
      return ClickMode.RIGHT;
    }
    try {
      return ClickMode.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new InvalidHuntConfigException(
          "Invalid click-mode: " + value + " (expected LEFT, RIGHT, or BOTH)");
    }
  }

  private static Instant parseSchedule(String value) throws InvalidHuntConfigException {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(value);
    } catch (DateTimeParseException ignored) {
      try {
        return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
      } catch (DateTimeParseException ex) {
        throw new InvalidHuntConfigException("Invalid schedule datetime: " + value);
      }
    }
  }
}
