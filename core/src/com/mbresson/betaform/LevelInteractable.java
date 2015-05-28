package com.mbresson.betaform;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * LevelInteractable objects can be interacted with when
 * the user is in contact with them and has pressed the action key.
 */
interface LevelInteractable extends CanHandlePlayerContact {

  /**
   * Must be called whenever the player presses the action key on the object.
   *
   * @return true when the interaction is finished
   */
  public boolean interact();

  /**
   * Must be called every frame.
   */
  public void displayInteraction(SpriteBatch batch);

  /**
   * Reset the state of the interactable object.
   * Called whenever the user stops interacting after it.
   */
  public void reset();

}

