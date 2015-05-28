package com.mbresson.betaform;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * This class is related to LevelAddon:
 * LevelAddon represents an addon object on the map,
 * PlayerAddon represents an addon owned by the player.
 */
enum PlayerAddon implements Json.Serializable {

  ADDON_SHIELD(LevelAddon.ADDON_SHIELD),
  ADDON_SPEED(LevelAddon.ADDON_SPEED),
  ADDON_STRENGTH(LevelAddon.ADDON_STRENGTH);

  private String type;

  private PlayerAddon(String type) {
    this.type = type;
    this.battery = MAX_BATTERY;
  }

  public static PlayerAddon findByName(String name) {
    for(PlayerAddon type: values()) {
      if(type.type.equals(name)) {
        return type;
      }
    }

    throw new IllegalArgumentException("No type for the Addon named " + name);
  }

  public String getLabel() {
    return this.type;
  }

  public static final int MAX_BATTERY = 1000;

  private int battery = MAX_BATTERY;

  public int getBattery() {
    return this.battery;
  }

  public void setBatteryToMax() {
    this.battery = MAX_BATTERY;
  }

  public void setBattery(int battery) {
    this.battery = battery;
  }

  public void increaseBattery(int amount) {
    this.battery = Math.min(this.battery + amount, MAX_BATTERY);
  }

  /**
   * @return true if the addon still has enough battery power
   */
  public boolean decreaseBattery(int amount) {
    this.battery -= amount*3;

    return this.battery > 0;
  }

  public static PlayerAddon newFromLevelAddon(LevelAddon levelAddon) {
    switch(levelAddon.getType()) {
      case LevelAddon.ADDON_SHIELD: return PlayerAddon.ADDON_SHIELD;
      case LevelAddon.ADDON_SPEED: return PlayerAddon.ADDON_SPEED;
      case LevelAddon.ADDON_STRENGTH: return PlayerAddon.ADDON_STRENGTH;
    }

    return null;
  }

  @Override
  public void write(Json json) {
    json.writeValue("type", this.type);
    json.writeValue("battery", this.battery);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    this.type = json.readValue(String.class, jsonMap.get("type"));
    this.battery = json.readValue(Integer.class, jsonMap.get("battery"));
  }

}

