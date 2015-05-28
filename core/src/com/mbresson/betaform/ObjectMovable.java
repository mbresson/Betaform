package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;

interface ObjectMovable {

  // x and y offsets in world's coordinates
  public void move(float x, float y);

  public void changeDirection(Direction direction);

  public void stop();

  public Vector2 getPhysicsPosition();

}

