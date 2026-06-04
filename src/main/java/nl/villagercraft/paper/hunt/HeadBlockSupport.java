package nl.villagercraft.paper.hunt;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;

public final class HeadBlockSupport {

  private static final int TARGET_RANGE = 5;

  private HeadBlockSupport() {}

  public static Optional<Block> targetBlock(Player player) {
    Block block = player.getTargetBlockExact(TARGET_RANGE);
    if (block == null || block.getType().isAir()) {
      return Optional.empty();
    }
    return Optional.of(block);
  }

  public static boolean isPlayerSkull(Block block) {
    Material type = block.getType();
    return type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD;
  }

  /** Profile UUID used as head {@code identity} when registering via {@code /headhunt add}. */
  public static Optional<UUID> profileUuid(Block block) {
    if (!(block.getState() instanceof Skull skull)) {
      return Optional.empty();
    }
    PlayerProfile profile = skull.getPlayerProfile();
    if (profile == null || profile.getUniqueId() == null) {
      return Optional.empty();
    }
    return Optional.of(profile.getUniqueId());
  }
}
