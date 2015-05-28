package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;

/**
 * A bonus is an object that can restore the player's health or battery
 * or increase his score.
 */
public class LevelBonus extends LevelArtifact implements CanHandlePlayerContact, Traversable {

  // constants for the type property
  public static final String BONUS_BATTERY = "battery";
  public static final String BONUS_HEALTH = "health";
  public static final String BONUS_SCORE = "score";

  public LevelBonus() {
    super();
    this.type = BONUS_BATTERY;
  }

  public LevelBonus(LevelBonus bonus) {
    super(bonus);
  }

  public LevelBonus(String type) {
    super();
    this.type = new String(type);
  }

  @Override
  public void handlePlayerContactBegin(Player player, Contact contact) {
    if(this.getUsed()) {
      return;
    }

    this.use();

    switch(this.getType()) {
      case LevelBonus.BONUS_BATTERY:
        player.increaseInnerBattery(Player.MAX_BATTERY / 2);
        break;

      case LevelBonus.BONUS_HEALTH:
        player.increaseHealth(Player.MAX_HEALTH / 2);
        break;

      case LevelBonus.BONUS_SCORE:
        player.increaseScore(1);
        break;
    }

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    gameRef.getLevel().removeObject(this);
  }

  @Override
  public void handlePlayerContactEnd(Player player, Contact contact) {
    // do nothing, as soon as a bonus is touched by the player, it disappears
  }

}


