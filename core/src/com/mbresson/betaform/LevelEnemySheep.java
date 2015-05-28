package com.mbresson.betaform;

public class LevelEnemySheep extends LevelEnemy {

  private static final String classID = LevelEnemySheep.class.getName();

  @Override
  protected int getLastRegionIndex() { return 4; }

  @Override
  protected String getPath() { return "img/sprites/robot-sheep.atlas"; }

  @Override
  protected int getMaxHealth() { return 10; }

  @Override
  public String getResourceEaterID() { return classID; }
}

