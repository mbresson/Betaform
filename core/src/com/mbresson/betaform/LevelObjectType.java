package com.mbresson.betaform;

enum LevelObjectType {
  BLOCK_MOVING("block-moving"),
  BLOCK_MOVABLE("block-movable"),
  ADDON("addon"),
  BONUS("bonus"),
  DOOR("door"),
  SWITCH("switch"),
  SAVE("save"),
  HINT("hint"),
  ROBOT_LION("enemy-lion"),
  ROBOT_SHEEP("enemy-sheep"),
  ROBOT_SHEEP_SMART("enemy-sheep-smart"),
  ROBOT_TREE("enemy-tree"),
  ROBOT_TRIPOD("enemy-tripod");

  private String type;

  private LevelObjectType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return type;
  }

  public Class<? extends LevelObject> getLevelObjectClass() throws IllegalArgumentException {
    switch(this) {
      case BLOCK_MOVING: return LevelBlockMoving.class;
      case BLOCK_MOVABLE: return LevelBlockMovable.class;
      case ADDON: return LevelAddon.class;
      case BONUS: return LevelBonus.class;
      case DOOR: return LevelDoor.class;
      case SWITCH: return LevelBlockSwitch.class;
      case SAVE: return LevelSave.class;
      case HINT: return LevelHint.class;
      case ROBOT_LION: return LevelEnemyLion.class;
      case ROBOT_SHEEP: return LevelEnemySheep.class;
      case ROBOT_SHEEP_SMART: return LevelEnemySmartSheep.class;
      case ROBOT_TREE: return LevelEnemyTree.class;
      case ROBOT_TRIPOD: return LevelEnemyTripod.class;
    }

    throw new IllegalArgumentException("No class for the object type named " + this.type);
  }
}

