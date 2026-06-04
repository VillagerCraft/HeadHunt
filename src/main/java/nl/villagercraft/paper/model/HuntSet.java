package nl.villagercraft.paper.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HuntSet {

  private String name;
  private Map<String, Head> heads = new LinkedHashMap<>();

  public HuntSet() {}

  public HuntSet(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Head> getHeads() {
    return heads;
  }

  public void setHeads(Map<String, Head> heads) {
    this.heads = heads;
  }
}
