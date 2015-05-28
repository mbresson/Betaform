package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class BehaviorStatic implements Behavior {

  // a static enemy cannot move but can shoot at the player
  public void behave(ObjectMovable movable) {
    GameInstance gameRef = Betaform.getGameRef();

    if(gameRef == null) {
      return;
    }

    float playerX = gameRef.getPlayer().getPhysicsPosition().x;
    float objectX = movable.getPhysicsPosition().x;

    if(playerX > objectX) {
      movable.changeDirection(Direction.RIGHT);
    } else {
      movable.changeDirection(Direction.LEFT);
    }
  }

  @Override
  public Vector2 getSpeed() { return new Vector2(); }

  @Override
  public void write(Json json) {
    json.writeValue("class", BehaviorType.STATIC.getLabel());
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
  }
}



