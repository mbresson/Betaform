package com.mbresson.betaform;

public class LevelEnemyTripod extends LevelEnemy {

  private static final String classID = LevelEnemyTripod.class.getName();

  @Override
  protected int getLastRegionIndex() { return 4; }

  @Override
  protected String getPath() { return "img/sprites/robot-tripod.atlas"; }

  @Override
  protected int getMaxHealth() { return 50; }

  @Override
  public String getResourceEaterID() { return classID; }
}


