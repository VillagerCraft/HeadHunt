package nl.villagercraft.paper.hunt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RegisterFindOutcome {

  public record ChatMessage(String messageKey, Map<String, String> placeholders) {}

  private final List<ChatMessage> playerMessages = new ArrayList<>();
  private final List<ChatMessage> broadcasts = new ArrayList<>();

  public void addPlayerMessage(String messageKey) {
    addPlayerMessage(messageKey, Map.of());
  }

  public void addPlayerMessage(String messageKey, Map<String, String> placeholders) {
    playerMessages.add(new ChatMessage(messageKey, placeholders));
  }

  public void addBroadcast(String messageKey, Map<String, String> placeholders) {
    broadcasts.add(new ChatMessage(messageKey, placeholders));
  }

  public List<ChatMessage> playerMessages() {
    return Collections.unmodifiableList(playerMessages);
  }

  public List<ChatMessage> broadcasts() {
    return Collections.unmodifiableList(broadcasts);
  }
}
