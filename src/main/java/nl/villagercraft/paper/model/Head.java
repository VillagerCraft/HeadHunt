package nl.villagercraft.paper.model;

/** Registered player skull at a fixed block. Lookup in v1 is by world + coordinates. */
public final class Head {

  private String name;
  private String setName;
  private String world;
  private int x;
  private int y;
  private int z;
  /** Profile UUID string from the skull at registration; part of stored head record (v2 may use in key). */
  private String identity;

  public Head() {}

  public Head(
      String name, String setName, String world, int x, int y, int z, String identity) {
    this.name = name;
    this.setName = setName;
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
    this.identity = identity;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSetName() {
    return setName;
  }

  public void setSetName(String setName) {
    this.setName = setName;
  }

  public String getWorld() {
    return world;
  }

  public void setWorld(String world) {
    this.world = world;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getZ() {
    return z;
  }

  public void setZ(int z) {
    this.z = z;
  }

  public String getIdentity() {
    return identity;
  }

  public void setIdentity(String identity) {
    this.identity = identity;
  }
}
