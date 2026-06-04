package nl.villagercraft.paper.model;

public enum ClickMode {
  LEFT,
  RIGHT,
  BOTH;

  public static ClickMode fromString(String value) {
    if (value == null || value.isBlank()) {
      return RIGHT;
    }
    return ClickMode.valueOf(value.trim().toUpperCase());
  }
}
