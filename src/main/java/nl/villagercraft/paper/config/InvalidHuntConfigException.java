package nl.villagercraft.paper.config;

public final class InvalidHuntConfigException extends Exception {

  private final String reason;

  public InvalidHuntConfigException(String reason) {
    super(reason);
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }
}
