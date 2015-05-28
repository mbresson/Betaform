package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;

/**
 * An addon is an object the player can collect
 * which gives him a special power (e.g. moving faster).
 */
public class LevelAddon extends LevelArtifact implements CanHandlePlayerContact, Traversable {

  // constants for the type property
  public static final String ADDON_SHIELD = "shield";
  public static final String ADDON_SPEED = "speed";
  public static final String ADDON_STRENGTH = "strength";

  public LevelAddon() {
    super();
    this.type = ADDON_SHIELD;
  }

  public LevelAddon(LevelAddon addon) {
    super(addon);
  }

  public LevelAddon(String type) {
    super();
    this.type = new String(type);
  }

  @Override
  public void handlePlayerContactBegin(Player player, Contact contact) {
    if(this.getUsed()) {
      return;
    }

    this.use();

    player.addAddon(PlayerAddon.newFromLevelAddon(this));

    GameInstance gameRef = Betaform.getGameRef();

    if(gameRef == null) {
      return;
    }

    gameRef.getLevel().removeObject(this);
  }

  @Override
  public void handlePlayerContactEnd(Player player, Contact contact) {
    // do nothing, as soon as an addon is touched by the player, it disappears
  }
}

