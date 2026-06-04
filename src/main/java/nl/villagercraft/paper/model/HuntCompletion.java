package nl.villagercraft.paper.model;

import java.time.Instant;
import java.util.UUID;

/** First hunt completion for a player; timestamp used for leaderboard tie-breaks. */
public final class HuntCompletion {

  private String hunt;
  private UUID player;
  private Instant timestamp;

  public HuntCompletion() {}

  public HuntCompletion(String hunt, UUID player, Instant timestamp) {
    this.hunt = hunt;
    this.player = player;
    this.timestamp = timestamp;
  }

  public String getHunt() {
    return hunt;
  }

  public void setHunt(String hunt) {
    this.hunt = hunt;
  }

  public UUID getPlayer() {
    return player;
  }

  public void setPlayer(UUID player) {
    this.player = player;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
