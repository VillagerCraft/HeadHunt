package nl.villagercraft.paper.model;

public final class BroadcastConfig {

  private boolean setComplete;
  private boolean huntComplete;

  public BroadcastConfig() {}

  public BroadcastConfig(boolean setComplete, boolean huntComplete) {
    this.setComplete = setComplete;
    this.huntComplete = huntComplete;
  }

  public boolean isSetComplete() {
    return setComplete;
  }

  public void setSetComplete(boolean setComplete) {
    this.setComplete = setComplete;
  }

  public boolean isHuntComplete() {
    return huntComplete;
  }

  public void setHuntComplete(boolean huntComplete) {
    this.huntComplete = huntComplete;
  }
}
