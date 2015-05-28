package com.mbresson.betaform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.EnumMap;

public class EditorScreen implements Screen, InputProcessor, GameInstance {

  private static final float CURSOR_SPEED = 50f;

  private static enum CursorMove {
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN
  }

  private static enum ActionType {
    ADD,
    MOVE,
    DELETE,
    EDIT;

    public ActionType getNext() {
      return values()[ (ordinal()+1) % values().length ];
    }

    public ActionType getPrevious() {
      int newOrdinal = this.ordinal() - 1;
      if(newOrdinal < 0) {
        newOrdinal = values().length - 1;
      }

      return values()[ newOrdinal ];
    }

    public static ActionType fromKey(int keycode) throws IllegalArgumentException {
      switch(keycode) {
        case Keys.A: return ADD;
        case Keys.M: return MOVE;
        case Keys.D: return DELETE;
        case Keys.E: return EDIT;
      }

      throw new IllegalArgumentException("No action for the key of code " + keycode);
    }

    public static void printExplanations() {
      System.out.println(
        "Press TAB to change the action mode,\n" +
        "or press one of the following shortcut keys:\n" +
        "\t- A for ADD (add a new object)\n" +
        "\t- M for MOVE (move an existing object)\n" +
        "\t- D for DELETE (delete an existing object)\n" +
        "\t- E for EDIT (edit the settings of an existing object)\n" +
        "\nPress T to switch between tiles and objects"
      );
    }
  }

  private Betaform betaformRef;

  private Skin skin;
  private Label levelInfoLabel;

  /*
   * The camera is always centered on the mouse.
   *
   * The static camera is used to display the gui.
   */
  private OrthographicCamera camera, staticCamera;
  private SpriteBatch cameraBatch, staticBatch;

  // the cursor is not the mouse cursor but an invisible cursor used to move around the level and followed by the camera
  private Vector2 cursorPosition = new Vector2();
  private EnumMap<CursorMove, Boolean> cursorMoves;

  private ActionType currentAction = ActionType.ADD;

  private World world;

  private Level level = new Level();

  private EditablesManager editablesManager = new EditablesManager();

  public EditorScreen(Betaform betaformRef) {
    Betaform.setGameRef(this);

    this.betaformRef = betaformRef;

    this.cursorMoves = new EnumMap<>(CursorMove.class);
    for(CursorMove move: CursorMove.values()) {
      this.cursorMoves.put(move, false);
    }

    this.create();
  }

  private void create() {
    this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

    this.levelInfoLabel = new Label("", this.skin);
    this.levelInfoLabel.setPosition(0, 20);

    this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.cameraBatch = new SpriteBatch();

    this.staticCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    this.staticCamera.translate(this.staticCamera.viewportWidth / 2, this.staticCamera.viewportHeight / 2);
    this.staticBatch = new SpriteBatch();

    this.world = new World(new Vector2(0, 0), true);

    this.camera.translate(this.camera.viewportWidth / 2, this.camera.viewportHeight / 2);

    this.cursorPosition.x = Gdx.graphics.getWidth()/2;
    this.cursorPosition.y = Gdx.graphics.getHeight()/2;
  }

  /**
   * @return the position of the mouse in the level
   */
  private Vector2 getMouseLevelPosition() {
    return new Vector2(
      (int)(this.cursorPosition.x - Gdx.graphics.getWidth()/2 + Gdx.input.getX()),
      (int)(this.cursorPosition.y + Gdx.graphics.getHeight()/2 - Gdx.input.getY())
    );
  }

  private void updateLevelInfoLabel() {
    /*
     * Display:
     * - The name of the level
     * - The size of the level (in pixels)
     * - The position of the mouse in the level
     */
    Vector2 mousePosition = this.getMouseLevelPosition();

    this.levelInfoLabel.setText(
      "\"" + this.level.getLevelName() + "\" | " +
      this.level.getWidth() + "x" + this.level.getHeight() + " | " +
      "@ " + (int)mousePosition.x + ":" +
      (int)mousePosition.y + " | " +
      "(" + this.currentAction + ")"
    );
  }

  public void setLevel(Level level) {
    this.level = level;

    try {
      this.level.preloadResources();
      this.editablesManager.preloadResources();
    } catch (ResourceLoader.AlreadyPreloadedException exception) {
      exception.printStackTrace();
      Gdx.app.exit();
    }

    ResourceLoader loader = ResourceLoader.getInstance();

    while(!loader.getManager().update()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // do nothing
      }
      System.out.print("\r" + loader.getManager().getProgress()*100 + "%");
    }
    System.out.println();

    try {
      this.level.postloadResources();
      this.editablesManager.postloadResources();
    } catch (ResourceLoader.NotPreloadedYetException exception) {
      exception.printStackTrace();
      Gdx.app.exit();
    }
  }

  private void moveCursor() {
    if(this.cursorMoves.get(CursorMove.MOVE_LEFT)) {
      this.cursorPosition.x -= CURSOR_SPEED;
      if(this.cursorPosition.x < Gdx.graphics.getWidth() / 2) {
        this.cursorPosition.x = Gdx.graphics.getWidth() / 2;
      }
    }

    if(this.cursorMoves.get(CursorMove.MOVE_RIGHT)) {
      this.cursorPosition.x += CURSOR_SPEED;
      if(this.cursorPosition.x > this.level.getWidth() - Gdx.graphics.getWidth() / 2) {
        this.cursorPosition.x = this.level.getWidth() - Gdx.graphics.getWidth() / 2;
      }
    }

    if(this.cursorMoves.get(CursorMove.MOVE_DOWN)) {
      this.cursorPosition.y -= CURSOR_SPEED;
      if(this.cursorPosition.y < Gdx.graphics.getHeight() / 2) {
        this.cursorPosition.y = Gdx.graphics.getHeight() / 2;
      }
    }

    if(this.cursorMoves.get(CursorMove.MOVE_UP)) {
      this.cursorPosition.y += CURSOR_SPEED;
      if(this.cursorPosition.y > this.level.getHeight() - Gdx.graphics.getHeight() / 2) {
        this.cursorPosition.y = this.level.getHeight() - Gdx.graphics.getHeight() / 2;
      }
    }

  }

  @Override
  public void render(float delta) {
		Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    this.moveCursor();
    this.updateLevelInfoLabel();

    this.camera.position.set(this.cursorPosition.x, this.cursorPosition.y, 0);
    this.level.getBoundaries().limitCamera(this.camera);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    this.camera.update();
    this.cameraBatch.setProjectionMatrix(this.camera.combined);

    this.cameraBatch.enableBlending();
		this.cameraBatch.begin();

      this.level.display(this.cameraBatch);
      if(this.currentAction == ActionType.ADD) {
        Vector2 mousePosition = this.getMouseLevelPosition();

        this.editablesManager.displayEditable(this.cameraBatch, mousePosition.x, mousePosition.y);
      }

		this.cameraBatch.end();

    this.staticCamera.update();
    this.staticBatch.setProjectionMatrix(this.staticCamera.combined);
    this.staticBatch.begin();

      this.levelInfoLabel.draw(this.staticBatch, 1f);

    this.staticBatch.end();
  }

  @Override
  public void resize(int width, int height) {
  }

  @Override
  public void show() {
    ActionType.printExplanations();

    this.cursorPosition.x = Gdx.graphics.getWidth()/2;
    this.cursorPosition.y = Gdx.graphics.getHeight()/2;

    Betaform.setGameRef(this);

    Gdx.input.setInputProcessor(this);
  }

  @Override
  public void hide() {
  }

  @Override
  public void pause() {
  }

  @Override
  public void resume() {
  }

  @Override
  public void dispose() {
    this.skin.dispose();

    this.cameraBatch.dispose();
    this.staticBatch.dispose();
  }

  @Override
  public boolean keyDown(int keycode) {
    switch(keycode) {
      case Configuration.Controls.KEY_MOVE_LEFT:
        this.cursorMoves.put(CursorMove.MOVE_LEFT, true);
        break;

      case Configuration.Controls.KEY_MOVE_RIGHT:
        this.cursorMoves.put(CursorMove.MOVE_RIGHT, true);
        break;

      case Configuration.Controls.KEY_MOVE_UP:
        this.cursorMoves.put(CursorMove.MOVE_UP, true);
        break;

      case Configuration.Controls.KEY_MOVE_DOWN:
        this.cursorMoves.put(CursorMove.MOVE_DOWN, true);
        break;
    }

    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    switch(keycode) {
      case Configuration.Controls.KEY_LEAVE:
        this.leave();
        break;

      case Keys.ENTER:
        this.level.writeToFile();
        break;

      case Configuration.Controls.KEY_MOVE_LEFT:
        this.cursorMoves.put(CursorMove.MOVE_LEFT, false);
        break;

      case Configuration.Controls.KEY_MOVE_RIGHT:
        this.cursorMoves.put(CursorMove.MOVE_RIGHT, false);
        break;

      case Configuration.Controls.KEY_MOVE_UP:
        this.cursorMoves.put(CursorMove.MOVE_UP, false);
        break;

      case Configuration.Controls.KEY_MOVE_DOWN:
        this.cursorMoves.put(CursorMove.MOVE_DOWN, false);
        break;

      case Keys.TAB:
        boolean backwards = 
          Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ||
          Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);

        if(backwards) {
          this.currentAction = this.currentAction.getPrevious();
        } else {
          this.currentAction = this.currentAction.getNext();
        }
        break;

      case Keys.T:
        this.editablesManager.switchMode();
        break;

      default:
        try {
          ActionType nextAction = ActionType.fromKey(keycode);
          this.currentAction = nextAction;
        } catch(IllegalArgumentException exception) {}
        break;

    }

    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    this.editablesManager.addEditable(this.level, this.getMouseLevelPosition());
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    if(amount > 0) {
      this.editablesManager.nextEditable();
    } else {
      this.editablesManager.previousEditable();
    }

    return false;
  }

  private void leave() {
    this.betaformRef.mainMenu();
  }

  @Override
  public World getWorld() {
    return this.world;
  }

  /**
   * This function is a stub because when running the editor,
   * no object will need to call it.
   */
  @Override
  public Player getPlayer() {
    return null;
  }

  /**
   * This function is a stub because when running the editor,
   * no object will need to call it.
   */
  @Override
  public BulletManager getBulletManager() {
    return null;
  }

  @Override
  public Level getLevel() {
    return this.level;
  }

  /**
   * This function is a stub because when running the editor,
   * no object will need to call it.
   */
  @Override
  public void setPaused(boolean paused) {
  }

  /**
   * This function is a stub because when running the editor,
   * no object will need to call it.
   */
  @Override
  public void leaveGame() {
  }

  /**
   * This function is a stub because when running the editor,
   * no object will need to call it.
   */
  @Override
  public void nextLevel() {
  }
}

