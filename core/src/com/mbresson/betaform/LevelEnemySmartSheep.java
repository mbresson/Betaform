package com.mbresson.betaform;

public class LevelEnemySmartSheep extends LevelEnemy {

  private static final String classID = LevelEnemySmartSheep.class.getName();

  @Override
  protected int getLastRegionIndex() { return 4; }

  @Override
  protected String getPath() { return "img/sprites/robot-sheep-smart.atlas"; }

  @Override
  protected int getMaxHealth() { return 20; }

  @Override
  public String getResourceEaterID() { return classID; }
}


