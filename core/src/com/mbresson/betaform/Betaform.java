package com.mbresson.betaform;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;

public class Betaform extends Game {

  private MainMenuScreen mainMenuScreen;
  private GameScreen currentGameScreen;
  private EditorScreen editorScreen;
  private EditorLauncherScreen editorLauncherScreen;

  private ResourceLoader resourceLoader;

  private static GameInstance gameRef;

  @Override
  public void create() {
    Box2D.init();

    this.mainMenuScreen = new MainMenuScreen(this);
    this.editorScreen = new EditorScreen(this);
    this.editorLauncherScreen = new EditorLauncherScreen(this);

    this.resourceLoader = ResourceLoader.getInstance();

    this.setScreen(this.mainMenuScreen);
  }

  public void exit() {
    Gdx.app.exit();
  }

  @Override
  public void dispose() {
    this.resourceLoader.dispose();

    this.mainMenuScreen.dispose();
    this.editorScreen.dispose();
    this.editorLauncherScreen.dispose();

    if(this.currentGameScreen != null) {
      this.currentGameScreen.dispose();
    }
  }

  public void newGame() {
    if(this.currentGameScreen != null) {
      this.currentGameScreen.dispose();
    }

    this.currentGameScreen = new GameScreen(this);

    this.setScreen(this.currentGameScreen);
  }

  /**
   * This function is exclusively used to go from one level to the next level.
   * The save is only used to keep the player's status information (addons, health, etc)
   * but his position will be changed to be the position of the entrance to the next level.
   */
  public void newGame(String levelName, Save save) {
    if(this.currentGameScreen != null) {
      this.currentGameScreen.dispose();
    }

    this.currentGameScreen = new GameScreen(this, save, true);

    this.setScreen(this.currentGameScreen);
  }

  public void loadGame() {
    if(Save.saveExists()) {
      Save save = Save.readFromFile();

      if(this.currentGameScreen != null) {
        this.currentGameScreen.dispose();
      }

      this.currentGameScreen = new GameScreen(this, save, false);

      this.setScreen(this.currentGameScreen);
    }
  }

  public void editor(Level level) {
    Betaform.setGameRef(this.editorScreen);

    this.editorScreen.setLevel(level);
    this.setScreen(this.editorScreen);
  }

  public void editorLauncher() {
    Betaform.gameRef = null;

    this.setScreen(this.editorLauncherScreen);
  }

  public void mainMenu() {
    Betaform.gameRef = null;

    this.setScreen(this.mainMenuScreen);
  }

  public static void setGameRef(GameInstance gameRef) {
    Betaform.gameRef = gameRef;
  }

  /**
   * @return null if no game is running, otherwise returns a reference to it
   *
   * Whenever this function is called, the caller must ensure its return isn't null before using it.
   * It is not advised to store the result of this function anywhere outside a function scope,
   * as the object it points to may be destroyed any time.
   */
  public static GameInstance getGameRef() {
    return Betaform.gameRef;
  }
}

