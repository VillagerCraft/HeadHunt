package nl.villagercraft.paper.storage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.villagercraft.paper.model.HuntCompletion;

public interface HuntCompletionRepository {

  List<HuntCompletion> getCompletionsForHunt(String hunt);

  Optional<HuntCompletion> getCompletion(String hunt, UUID player);

  boolean hasCompletion(String hunt, UUID player);

  void recordCompletion(HuntCompletion completion);

  void removeCompletion(String hunt, UUID player);

  void removeCompletionsForHunt(String hunt);

  void clear();

  void load();

  void save();
}
