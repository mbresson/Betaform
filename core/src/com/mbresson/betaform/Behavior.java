package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;

interface Behavior extends Json.Serializable {

  /*
   * This function is called every frame
   * and is used to move the object accordingly.
   */
  public void behave(ObjectMovable movable);

  public Vector2 getSpeed();

}

