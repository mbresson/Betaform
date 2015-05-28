package com.mbresson.betaform;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class LevelBlockSwitch extends LevelBlock implements CanBeHitByBullet {

  public String getType() { return "switch-" + this.state.getLabel(); } 

  public enum SwitchState {
    ON("on"),
    OFF("off");

    private String type;

    private SwitchState(String type) {
      this.type = type;
    }

    public String getLabel() {
      return this.type;
    }
  }

  private SwitchState state = SwitchState.OFF;
  private int door = 0;
  private LevelDoor doorRef = null;

  private float timeout = 0;
  private float timerTime = 0f;

  public LevelBlockSwitch() {
    super();
    this.type = "switch-off";
  }

  public LevelBlockSwitch(LevelBlockSwitch block) {
    super(block);

    this.door = block.door;
    this.timeout = block.timeout;
  }

  public int getDoor() {
    return this.door;
  }

  public void setState(SwitchState state) {
    this.state = state;
    this.sprite.setRegion(textureAtlas.findRegion(this.getType()));

    if(state == SwitchState.ON) {
      doorRef.increaseNumOpenersOn();
    } else {
      doorRef.decreaseNumOpenersOn();
    }
  }

  public void setDoorRef(LevelDoor door) {
    this.doorRef = door;
  }

  public void invertState() {
    this.setState(
      (this.state == SwitchState.ON ? SwitchState.OFF : SwitchState.ON)
    );
  }

  /**
   * @return always true because a block cannot be dead
   */
  @Override
  public boolean hitBy(BulletManager.Bullet bullet) {
    this.invertState();

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return true;
    }

    gameRef.getBulletManager().destroyBullet(bullet);

    return true;
  }

  public void update(float deltaTime) {
    super.update(deltaTime);

    if(this.state == SwitchState.ON) {
      this.timerTime += deltaTime;
      if(this.timerTime > this.timeout) {
        this.setState(SwitchState.OFF);

        this.timerTime = 0f;
      }
    }
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("door", this.door);
    json.writeValue("timeout", (int)this.timeout);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }
}

