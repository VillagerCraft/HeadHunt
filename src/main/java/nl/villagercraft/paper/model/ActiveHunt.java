package nl.villagercraft.paper.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ActiveHunt {

  private String name;
  private boolean activated;
  private boolean paused;
  private String timezone;
  private Instant scheduleStart;
  private Instant scheduleEnd;
  private ClickMode clickMode;
  private RewardConfig rewards = new RewardConfig();
  private BroadcastConfig broadcast = new BroadcastConfig();
  private Map<String, HuntSet> sets = new LinkedHashMap<>();
  private Set<UUID> banlist = new LinkedHashSet<>();

  public ActiveHunt() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(boolean activated) {
    this.activated = activated;
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public Instant getScheduleStart() {
    return scheduleStart;
  }

  public void setScheduleStart(Instant scheduleStart) {
    this.scheduleStart = scheduleStart;
  }

  public Instant getScheduleEnd() {
    return scheduleEnd;
  }

  public void setScheduleEnd(Instant scheduleEnd) {
    this.scheduleEnd = scheduleEnd;
  }

  public ClickMode getClickMode() {
    return clickMode;
  }

  public void setClickMode(ClickMode clickMode) {
    this.clickMode = clickMode;
  }

  public RewardConfig getRewards() {
    return rewards;
  }

  public void setRewards(RewardConfig rewards) {
    this.rewards = rewards;
  }

  public BroadcastConfig getBroadcast() {
    return broadcast;
  }

  public void setBroadcast(BroadcastConfig broadcast) {
    this.broadcast = broadcast;
  }

  public Map<String, HuntSet> getSets() {
    return sets;
  }

  public void setSets(Map<String, HuntSet> sets) {
    this.sets = sets;
  }

  public Set<UUID> getBanlist() {
    return banlist;
  }

  public void setBanlist(Set<UUID> banlist) {
    this.banlist = banlist;
  }
}
