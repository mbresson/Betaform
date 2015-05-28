package com.mbresson.betaform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

/**
 * A save is an object allowing to save the position
 * when the user presses the action key on it.
 */
public class LevelSave extends LevelHelper implements LevelInteractable {

  private static final String classID = LevelSave.class.getName();

  private boolean interacting = false;

  @Override
  public String getType() { return "save"; }

  public LevelSave() {
  }

  public LevelSave(LevelSave save) {
    super(save);
  }

  public void displayInteraction(SpriteBatch batch) {
  }

  public void reset() {
    /*
     * We start at -1 because the index is incremented at the start of every interaction.
     * If we set the index to 0 initially, it will be set to 1 after the first interaction
     * (when the user first presses the action key to see the help text)
     * and so the displayInteraction function will never show the first help string (of index 0) but only the second.
     */
    this.interacting = false;
  }

  @Override
  public boolean interact() {
    GameInstance game = Betaform.getGameRef();

    Save save = new Save(game.getPlayer().createSave(), game.getLevel().getLevelName());

    save.persist();
    return true;
  }

  @Override
  public void handlePlayerContactBegin(Player player, Contact contact) {
    player.addInteractable(this);
  }

  @Override
  public void handlePlayerContactEnd(Player player, Contact contact) {
    player.removeInteractable(this);
    this.reset();
  }

  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
    super.preloadResources();
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    super.postloadResources();
  }

  @Override
  public String getResourceEaterID() { return classID; }
}


