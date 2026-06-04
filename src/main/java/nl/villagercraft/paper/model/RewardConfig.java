package nl.villagercraft.paper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RewardConfig {

  private List<String> headDefault = new ArrayList<>();
  private Map<String, List<String>> byHead = new HashMap<>();
  private List<String> setDefault = new ArrayList<>();
  private Map<String, List<String>> bySet = new HashMap<>();
  private List<String> huntCommands = new ArrayList<>();

  public RewardConfig() {}

  public List<String> getHeadDefault() {
    return headDefault;
  }

  public void setHeadDefault(List<String> headDefault) {
    this.headDefault = headDefault;
  }

  public Map<String, List<String>> getByHead() {
    return byHead;
  }

  public void setByHead(Map<String, List<String>> byHead) {
    this.byHead = byHead;
  }

  public List<String> getSetDefault() {
    return setDefault;
  }

  public void setSetDefault(List<String> setDefault) {
    this.setDefault = setDefault;
  }

  public Map<String, List<String>> getBySet() {
    return bySet;
  }

  public void setBySet(Map<String, List<String>> bySet) {
    this.bySet = bySet;
  }

  public List<String> getHuntCommands() {
    return huntCommands;
  }

  public void setHuntCommands(List<String> huntCommands) {
    this.huntCommands = huntCommands;
  }
}
