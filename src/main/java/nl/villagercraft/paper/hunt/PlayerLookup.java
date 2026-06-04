package nl.villagercraft.paper.hunt;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/** Resolves player names to UUIDs for online and cached offline profiles. */
public final class PlayerLookup {

  private PlayerLookup() {}

  /** Resolved player identity for commands and messages. */
  public record ResolvedPlayer(UUID uuid, String displayName) {}

  /** Resolves an online player by name (case-insensitive). */
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

  /**
   * Resolves a known player: online first, then Paper/Bukkit name cache (has played before).
   * Does not create profiles for names that never joined the server.
   */
  public static Optional<ResolvedPlayer> resolveKnownPlayer(String name) {
    Optional<Player> online = onlinePlayer(name);
    if (online.isPresent()) {
      Player player = online.get();
      return Optional.of(new ResolvedPlayer(player.getUniqueId(), player.getName()));
    }

    OfflinePlayer cached = offlinePlayerIfCached(name);
    if (cached != null) {
      return toResolved(cached, name);
    }
    return Optional.empty();
  }

  private static OfflinePlayer offlinePlayerIfCached(String name) {
    OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
    if (cached != null) {
      return cached;
    }
    if (!name.equalsIgnoreCase(name.toLowerCase())) {
      return Bukkit.getOfflinePlayerIfCached(name.toLowerCase());
    }
    return null;
  }

  private static Optional<ResolvedPlayer> toResolved(OfflinePlayer offline, String fallbackName) {
    if (!offline.hasPlayedBefore()) {
      return Optional.empty();
    }
    String displayName = offline.getName() != null ? offline.getName() : fallbackName;
    return Optional.of(new ResolvedPlayer(offline.getUniqueId(), displayName));
  }
}
