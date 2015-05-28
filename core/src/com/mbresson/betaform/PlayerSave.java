package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.JsonIterator;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * This class holds information pertaining to the player
 * that will need to be written to and read from save files.
 */
public class PlayerSave implements Json.Serializable {

  private Array<PlayerAddon> addons;
  private int currentAddon = -1;
  private int health, score, battery;
  private Vector2 physicsPosition;

  public Array<PlayerAddon> getAddons() {
    return this.addons;
  }

  public int getCurrentAddon() {
    return this.currentAddon;
  }

  public int getHealth() {
    return this.health;
  }

  public int getScore() {
    return this.score;
  }

  public int getBattery() {
    return this.battery;
  }

  public Vector2 getPhysicsPosition() {
    return this.physicsPosition;
  }

  public void setPhysicsPosition(Vector2 physicsPosition) {
    this.physicsPosition = physicsPosition;
  }

  public PlayerSave() {
    this.addons = new Array<>(10);
    this.currentAddon = -1;
    this.health = Player.MAX_HEALTH;
    this.score = 0;
    this.battery = Player.MAX_BATTERY;
    this.physicsPosition = new Vector2();
  }

  public PlayerSave(Array<PlayerAddon> addons, int currentAddon, int health, int score, int battery, Vector2 physicsPosition) {
    this.addons = addons;
    this.currentAddon = currentAddon;
    this.health = health;
    this.score = score;
    this.battery = battery;
    this.physicsPosition = physicsPosition;
  }

  @Override
  public void write(Json json) {
    json.writeValue("addons", this.addons);
    json.writeValue("currentAddon", this.currentAddon);
    json.writeValue("position", this.physicsPosition);
    json.writeValue("health", this.health);
    json.writeValue("score", this.score);
    json.writeValue("battery", this.battery);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    this.health = json.readValue(Integer.class, jsonMap.get("health"));
    this.score = json.readValue(Integer.class, jsonMap.get("score"));
    this.battery = json.readValue(Integer.class, jsonMap.get("battery"));
    this.currentAddon = json.readValue(Integer.class, jsonMap.get("currentAddon"));
    this.physicsPosition = json.readValue(Vector2.class, jsonMap.get("position"));

    this.addons = new Array<>(10);

    for(JsonValue value: jsonMap.get("addons").iterator()) {
      String type =  json.readValue(String.class, value.get("type"));
      int battery = json.readValue(Integer.class, value.get("battery"));

      PlayerAddon newAddon = PlayerAddon.findByName(type);
      newAddon.setBattery(battery);

      this.addons.add(newAddon);
    }
  }
}

