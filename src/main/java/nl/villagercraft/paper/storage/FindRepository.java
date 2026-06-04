package nl.villagercraft.paper.storage;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import nl.villagercraft.paper.model.Find;

public interface FindRepository {

  List<Find> getFinds();

  List<Find> getFindsForHunt(String hunt);

  List<Find> getFindsForHead(String headName);

  int countDistinctPlayersForHead(String hunt, String headName);

  int countFindsForPlayer(String hunt, UUID player);

  int countFindsForPlayerInSet(String hunt, UUID player, Collection<String> headNames);

  boolean hasFind(String hunt, UUID player, String head);

  void addFind(Find find);

  void removeFindsForHead(String headName);

  void removeFindsForHunt(String hunt);

  void removeFindsForPlayer(String hunt, UUID player);

  void removeFindsForPlayerInSet(String hunt, UUID player, Collection<String> headNames);

  void removeFindForPlayer(String hunt, UUID player, String headName);

  void clear();

  void load();

  void save();
}
