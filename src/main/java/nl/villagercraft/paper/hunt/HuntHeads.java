package nl.villagercraft.paper.hunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.Head;
import nl.villagercraft.paper.model.HuntSet;

public final class HuntHeads {

  private HuntHeads() {}

  public static int totalHeadCount(ActiveHunt hunt) {
    int total = 0;
    for (HuntSet set : hunt.getSets().values()) {
      total += set.getHeads().size();
    }
    return total;
  }

  public static List<Head> allHeads(ActiveHunt hunt) {
    List<Head> heads = new ArrayList<>();
    for (HuntSet set : hunt.getSets().values()) {
      heads.addAll(set.getHeads().values());
    }
    return heads;
  }

  public static Collection<String> headNamesInSet(ActiveHunt hunt, String setName) {
    HuntSet set = hunt.getSets().get(setName);
    if (set == null) {
      return List.of();
    }
    return set.getHeads().keySet();
  }
}
