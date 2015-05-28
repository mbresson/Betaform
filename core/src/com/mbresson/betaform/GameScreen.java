package com.mbresson.betaform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen, InputProcessor, GameInstance {

  private Betaform betaformRef;

  /*
   * The camera is always centered on the player.
   *
   * The static camera is used to display player health/score/battery status
   * and other information.
   */
  private OrthographicCamera camera, staticCamera;

  private SpriteBatch cameraBatch, staticBatch;

  private boolean paused = false;

  private World world;

  private Level level;
  private Player player;
  private BulletManager bulletManager;

  private Save save;

  // used to have a fixed timestep
  private double physicsTimeAccumulator = 0f, currentTime = 0f;

  public GameScreen(Betaform betaformRef) {
    Betaform.setGameRef(this);

    this.betaformRef = betaformRef;

    this.create(Configuration.Level.FIRST_LEVEL_NAME);
  }

  /**
   * @param start if true, the player starts at the entrance of the level (and the save position is erased). If false, he starts at the position saved.
   */
  public GameScreen(Betaform betaformRef, Save save, boolean start) {
    Betaform.setGameRef(this);

    this.betaformRef = betaformRef;

    this.create(save.getLevel());
    this.player.reloadFromSave(save.getPlayerSave());

    if(start) {
      this.player.resetInitialPosition(this.level.getEntrance().getPosition());
      save.getPlayerSave().setPhysicsPosition(this.player.getPhysicsPosition());
    }
  }

  private void create(String levelName) {
    this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    this.staticCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    this.camera.translate(this.camera.viewportWidth / 2, this.camera.viewportHeight / 2);
    this.staticCamera.translate(this.staticCamera.viewportWidth / 2, this.staticCamera.viewportHeight / 2);

		this.cameraBatch = new SpriteBatch();
    this.staticBatch = new SpriteBatch();

    this.world = new World(new Vector2(0, Configuration.Physics.GRAVITY), true);

    this.currentTime = TimeUtils.millis() / 1000.0;

    this.level = Level.read(levelName);

    this.bulletManager = new BulletManager();
    this.player = new Player(this.level.getEntrance().getPosition());

    try {
      this.bulletManager.preloadResources();
      this.player.preloadResources();
      this.level.preloadResources();
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
      this.bulletManager.postloadResources();
      this.player.postloadResources();
      this.level.postloadResources();
    } catch (ResourceLoader.NotPreloadedYetException exception) {
      exception.printStackTrace();
      Gdx.app.exit();
    }

    this.world.setContactListener(new ContactHandler());
  }

  private void doPhysicsStep() {
    double newTime = TimeUtils.millis() / 1000.0;
    double frameTime = Math.min(newTime - this.currentTime, 0.25);
    float deltaTime = (float)frameTime;

    this.currentTime = newTime;
    this.physicsTimeAccumulator += deltaTime;

    while(this.physicsTimeAccumulator >= Configuration.Physics.FRAMERATE_STEP) {
      this.world.step(Configuration.Physics.FRAMERATE_STEP, 6, 2);
      this.physicsTimeAccumulator -= Configuration.Physics.FRAMERATE_STEP;

      this.level.update(deltaTime);
      this.player.update(deltaTime);
      this.bulletManager.update(deltaTime);
    }
  }

  @Override
  public void render(float delta) {
		Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if(!this.paused) {
      this.camera.position.set(this.player.getCenterX(), this.player.getCenterY(), 0);
      this.level.getBoundaries().limitCamera(this.camera);
    }

    this.camera.update();
    this.cameraBatch.setProjectionMatrix(this.camera.combined);

    this.cameraBatch.enableBlending();
		this.cameraBatch.begin();

      this.level.display(this.cameraBatch);
      this.player.display(this.cameraBatch);
      this.bulletManager.display(this.cameraBatch);

		this.cameraBatch.end();

    this.staticCamera.update();
    this.staticBatch.setProjectionMatrix(this.staticCamera.combined);
    this.staticBatch.begin();

      this.player.displayOverlay(this.staticBatch);
      this.player.displayInteraction(this.staticBatch);

    this.staticBatch.end();

    if(!this.paused) {
      this.doPhysicsStep();
    }
  }

  @Override
  public void resize(int width, int height) {
  }

  @Override
  public void show() {
    Betaform.setGameRef(this);

    Gdx.input.setInputProcessor(this);
    Gdx.input.setCursorCatched(true);
  }

  @Override
  public void hide() {
    Gdx.input.setCursorCatched(false);
  }

  @Override
  public void pause() {
  }

  @Override
  public void resume() {
  }

  @Override
  public void dispose() {
    this.cameraBatch.dispose();
    this.staticBatch.dispose();

    this.world.dispose();
  }

  @Override
  public boolean keyDown(int keycode) {
    if(!this.paused) {
      switch(keycode) {
        case Configuration.Controls.KEY_MOVE_LEFT:
          this.player.startMoving(Direction.LEFT);
          break;

        case Configuration.Controls.KEY_MOVE_RIGHT:
          this.player.startMoving(Direction.RIGHT);
          break;

        case Configuration.Controls.KEY_JUMP:
          this.player.jump();
          break;

        case Keys.SHIFT_LEFT:
        case Keys.SHIFT_RIGHT:
          this.player.startPushing();
          break;
      }
    }

    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    switch(keycode) {
      case Configuration.Controls.KEY_ACTION:
        this.player.interact();
        break;

      case Configuration.Controls.KEY_LEAVE:
        this.leaveGame();
        break;

      case Configuration.Controls.KEY_PAUSE:
        if(!player.isInteracting()) {
          this.paused = !this.paused;
        }
        break;
    }

    if(!this.paused) {
      switch(keycode) {
        case Configuration.Controls.KEY_MOVE_LEFT:
          this.player.stopMoving(Direction.LEFT);
          break;

        case Configuration.Controls.KEY_MOVE_RIGHT:
          this.player.stopMoving(Direction.RIGHT);
          break;

        case Keys.TAB:
          boolean backwards = 
            Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ||
            Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);

          this.player.changeAddon(backwards);
          break;

        case Keys.SHIFT_LEFT:
        case Keys.SHIFT_RIGHT:
          this.player.stopPushing();
          break;

        case Keys.L:
          Save save = Save.readFromFile();

          if(save.getLevel().equals(this.level.getLevelName())) {
            this.player.reloadFromSave(save.getPlayerSave());
          }
          break;

        case Configuration.Controls.KEY_SHOOT:
          this.player.shoot();
          break;
      }
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
    return false;
  }

  @Override
  public World getWorld() {
    return this.world;
  }

  @Override
  public Player getPlayer() {
    return this.player;
  }

  @Override
  public BulletManager getBulletManager() {
    return this.bulletManager;
  }

  @Override
  public Level getLevel() {
    return this.level;
  }

  @Override
  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  @Override
  public void nextLevel() {
    Save save = new Save(this.player.createSave(), this.level.getNextlevel());
    this.betaformRef.newGame(this.level.getNextlevel(), save);
  }

  @Override
  public void leaveGame() {
    this.betaformRef.mainMenu();
  }
}

