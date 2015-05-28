package com.mbresson.betaform;

public class Vector2D {

  private int x, y;

  public Vector2D(Vector2D vector) {
    this.x = vector.x;
    this.y = vector.y;
  }

  public Vector2D(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Vector2D() {
    this(0, 0);
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

}

