package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class BehaviorNone implements Behavior {

  public void behave(ObjectMovable movable) {
    // do nothing!
  }

  @Override
  public Vector2 getSpeed() { return new Vector2(); }

  @Override
  public void write(Json json) {
    json.writeValue("class", BehaviorType.NONE.getLabel());
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
  }
}




