package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;

/**
 * This interface provides methods needed by game objects
 * (e.g. an enemy may need getPlayer() to inflict damage to the player).
 *
 * It is implemented by {@link com.mbresson.betaform.GameScreen } and {@ com.mbresson.betaform.EditorScreen },
 * although {@ com.mbresson.betaform.EditorScreen } provides only stubs for some methods (e.g. setPaused) which are useless for a level editor.
 */
interface GameInstance {

  public World getWorld();
  public Player getPlayer();
  public BulletManager getBulletManager();
  public Level getLevel();
  public void setPaused(boolean paused);
  public void leaveGame();
  public void nextLevel();

}

