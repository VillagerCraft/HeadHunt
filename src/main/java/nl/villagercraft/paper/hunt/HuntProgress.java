package nl.villagercraft.paper.hunt;

import java.util.UUID;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.HuntSet;
import nl.villagercraft.paper.storage.FindRepository;

/** Derives set/hunt completion from find records. Empty sets count as complete. */
public final class HuntProgress {

  private HuntProgress() {}

  public static boolean isSetComplete(
      ActiveHunt hunt, UUID player, String setName, FindRepository findRepository) {
    HuntSet set = hunt.getSets().get(setName);
    if (set == null) {
      return true;
    }
    int total = set.getHeads().size();
    if (total == 0) {
      return true;
    }
    int found =
        findRepository.countFindsForPlayerInSet(
            hunt.getName(), player, set.getHeads().keySet());
    return found >= total;
  }

  public static boolean isHuntComplete(
      ActiveHunt hunt, UUID player, FindRepository findRepository) {
    if (hunt.getSets().isEmpty()) {
      return false;
    }
    for (HuntSet set : hunt.getSets().values()) {
      if (!isSetComplete(hunt, player, set.getName(), findRepository)) {
        return false;
      }
    }
    return true;
  }

  /** True when this find is the one that completed the set (set must contain at least one head). */
  public static boolean setJustCompleted(
      ActiveHunt hunt, UUID player, String setName, FindRepository findRepository) {
    HuntSet set = hunt.getSets().get(setName);
    if (set == null || set.getHeads().isEmpty()) {
      return false;
    }
    int total = set.getHeads().size();
    int found =
        findRepository.countFindsForPlayerInSet(
            hunt.getName(), player, set.getHeads().keySet());
    return found == total;
  }

  public static boolean huntJustCompleted(
      ActiveHunt hunt,
      UUID player,
      String headSetName,
      FindRepository findRepository) {
    if (!isHuntComplete(hunt, player, findRepository)) {
      return false;
    }
    return !wasHuntCompleteBefore(hunt, player, headSetName, findRepository);
  }

  private static boolean wasHuntCompleteBefore(
      ActiveHunt hunt, UUID player, String headSetName, FindRepository findRepository) {
    if (hunt.getSets().isEmpty()) {
      return false;
    }
    for (HuntSet set : hunt.getSets().values()) {
      int total = set.getHeads().size();
      if (total == 0) {
        continue;
      }
      int found =
          findRepository.countFindsForPlayerInSet(
              hunt.getName(), player, set.getHeads().keySet());
      if (set.getName().equals(headSetName)) {
        found--;
      }
      if (found < total) {
        return false;
      }
    }
    return true;
  }
}
