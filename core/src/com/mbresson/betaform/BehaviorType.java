package com.mbresson.betaform;

enum BehaviorType {
  NONE("behavior-none"),
  AUTOMATED("behavior-automated"),
  STATIC("behavior-static");

  private String type;

  private BehaviorType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return type;
  }

  public Class<? extends Behavior> getBehaviorClass() throws IllegalArgumentException {
    switch(this) {
      case NONE: return BehaviorNone.class;
      case AUTOMATED: return BehaviorAutomated.class;
      case STATIC: return BehaviorStatic.class;
    }

    throw new IllegalArgumentException("No class for the behavior named " + this.type);
  }
}


