package com.mbresson.betaform;

public class LevelEnemyLion extends LevelEnemy {

  private static final String classID = LevelEnemyLion.class.getName();

  @Override
  protected int getLastRegionIndex() { return 5; }

  @Override
  protected String getPath() { return "img/sprites/robot-lion.atlas"; }

  @Override
  protected int getMaxHealth() { return 100; }

  @Override
  public String getResourceEaterID() { return classID; }

}


