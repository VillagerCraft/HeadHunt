package nl.villagercraft.paper.storage;

import java.util.Optional;
import nl.villagercraft.paper.model.ActiveHunt;

public interface ActiveHuntRepository {

  Optional<ActiveHunt> getActiveHunt();

  void setActiveHunt(ActiveHunt hunt);

  void clearActiveHunt();

  void load();

  void save();
}
