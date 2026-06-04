package nl.villagercraft.paper.hunt;

import java.util.Optional;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.Head;
import nl.villagercraft.paper.model.HuntSet;
import org.bukkit.block.Block;

public final class HeadRegistry {

  private HeadRegistry() {}

  public static Optional<Head> findByBlock(ActiveHunt hunt, Block block) {
    return findByLocation(
        hunt, block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
  }

  public static Optional<Head> findByLocation(
      ActiveHunt hunt, String world, int x, int y, int z) {
    for (HuntSet set : hunt.getSets().values()) {
      for (Head head : set.getHeads().values()) {
        if (matchesLocation(head, world, x, y, z)) {
          return Optional.of(head);
        }
      }
    }
    return Optional.empty();
  }

  public static boolean isHeadNameTaken(ActiveHunt hunt, String headName) {
    for (HuntSet set : hunt.getSets().values()) {
      if (set.getHeads().containsKey(headName)) {
        return true;
      }
    }
    return false;
  }

  public static Optional<HuntSet> findSet(ActiveHunt hunt, String setName) {
    return Optional.ofNullable(hunt.getSets().get(setName));
  }

  public static boolean removeHeadAtLocation(ActiveHunt hunt, String world, int x, int y, int z) {
    for (HuntSet set : hunt.getSets().values()) {
      String headName =
          set.getHeads().entrySet().stream()
              .filter(entry -> matchesLocation(entry.getValue(), world, x, y, z))
              .map(entry -> entry.getKey())
              .findFirst()
              .orElse(null);
      if (headName != null) {
        set.getHeads().remove(headName);
        return true;
      }
    }
    return false;
  }

  private static boolean matchesLocation(Head head, String world, int x, int y, int z) {
    return head.getWorld().equals(world)
        && head.getX() == x
        && head.getY() == y
        && head.getZ() == z;
  }
}
