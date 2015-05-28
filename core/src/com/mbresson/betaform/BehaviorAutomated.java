package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class BehaviorAutomated implements Behavior {

  Array<Vector2> moves = new Array<>();
  private Vector2 speed;

  // the current move
  private int moveIndex = 0;

  // how much have we moved since the start of the moveIndex-th move?
  private Vector2 moveAccumulator = new Vector2(0, 0);

  public BehaviorAutomated(Array<Vector2> moves) {
    this.moves = moves;
  }

  public BehaviorAutomated() {
    super();

    speed = new Vector2(10, 0);
  }

  public void behave(ObjectMovable movable) {
    Vector2 currentMove = this.moves.get(this.moveIndex);

    float physicsSpeedX = speed.x / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    float physicsSpeedY = speed.y / Configuration.Physics.WORLD_UNIT_TO_PIXELS;

    if(currentMove.x < 0) {
      physicsSpeedX = -physicsSpeedX;
    }

    if(currentMove.y < 0) {
      physicsSpeedY = -physicsSpeedY;
    }

    moveAccumulator.x += Math.abs(physicsSpeedX);
    moveAccumulator.y += Math.abs(physicsSpeedY);

    float moveX = 0, moveY = 0;

    if(moveAccumulator.x < Math.abs(currentMove.x)) {
      moveX = physicsSpeedX;
    }

    if(moveAccumulator.y < Math.abs(currentMove.y)) {
      moveY = physicsSpeedY;
    }

    movable.move(moveX, moveY);

    if(
      moveAccumulator.x >= Math.abs(currentMove.x) &&
      moveAccumulator.y >= Math.abs(currentMove.y)
    ) {
      moveAccumulator.x = 0;
      moveAccumulator.y = 0;

      moveIndex++;
      if(moveIndex >= this.moves.size) {
        moveIndex = 0;
      }
    }
  }

  public Vector2 getSpeed() {
    return this.speed;
  }

  @Override
  public void write(Json json) {

    for(BehaviorType behaviorType: BehaviorType.values()) {
      json.addClassTag(
        behaviorType.getLabel(),
        behaviorType.getBehaviorClass()
      );
    }

    json.writeValue("speed", this.speed);
    json.writeValue("moves", this.moves);
    json.writeValue("class", BehaviorType.AUTOMATED.getLabel());
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }
}

