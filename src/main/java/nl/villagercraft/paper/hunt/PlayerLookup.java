package nl.villagercraft.paper.hunt;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerLookup {

  private PlayerLookup() {}

  /** Resolves an online player by exact name (case-insensitive). */
  public static Optional<Player> onlinePlayer(String name) {
    Player player = Bukkit.getPlayerExact(name);
    if (player != null) {
      return Optional.of(player);
    }
    for (Player online : Bukkit.getOnlinePlayers()) {
      if (online.getName().equalsIgnoreCase(name)) {
        return Optional.of(online);
      }
    }
    return Optional.empty();
  }

  public static Optional<UUID> onlinePlayerUuid(String name) {
    return onlinePlayer(name).map(Player::getUniqueId);
  }
}
