package nl.villagercraft.paper.hunt;

import java.util.Map;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.Head;
import org.bukkit.entity.Player;

/**
 * Expands built-in reward command placeholders. Unknown placeholders are left as-is.
 *
 * <p>Supported: {@code {player}}, {@code {uuid}}, {@code {head}}, {@code {set}}, {@code {hunt}},
 * {@code {finds}}, {@code {set_finds}}, {@code {set_total}}.
 */
public final class RewardPlaceholderResolver {

  private static final String[] KNOWN_PLACEHOLDERS = {
    "player", "uuid", "head", "set", "hunt", "finds", "set_finds", "set_total"
  };

  private RewardPlaceholderResolver() {}

  public static String resolve(String template, RewardContext context) {
    String result = template;
    for (String key : KNOWN_PLACEHOLDERS) {
      result = result.replace("{" + key + "}", context.values().get(key));
    }
    return result;
  }

  public record RewardContext(Map<String, String> values) {

    public static RewardContext forFind(
        ActiveHunt hunt,
        Player player,
        Head head,
        int totalFinds,
        int setFinds,
        int setTotal) {
      return new RewardContext(
          Map.of(
              "player", player.getName(),
              "uuid", player.getUniqueId().toString(),
              "head", head.getName(),
              "set", head.getSetName(),
              "hunt", hunt.getName(),
              "finds", Integer.toString(totalFinds),
              "set_finds", Integer.toString(setFinds),
              "set_total", Integer.toString(setTotal)));
    }
  }
}
