package nl.villagercraft.paper.model;

import java.time.Instant;
import java.util.UUID;

public final class Find {

  private String hunt;
  private UUID player;
  private String head;
  private Instant timestamp;

  public Find() {}

  public Find(String hunt, UUID player, String head, Instant timestamp) {
    this.hunt = hunt;
    this.player = player;
    this.head = head;
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

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
