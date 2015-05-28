package com.mbresson.betaform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 * This class is used to display a simple form to the user
 * allowing him to create / open a level, specifying its name and size.
 */
public class EditorLauncherScreen implements Screen, InputProcessor {

  private static final float TABLE_CELL_WIDTH = 250f;
  private static final float TABLE_CELL_PADDING = TABLE_CELL_WIDTH / 10;

  private Betaform betaformRef;

  private Skin skin;
  private Stage stage;

  private Table newLevelTable;
  private TextField newLevelNameField;
  private Label newLevelLabel, newLevelNameLabel, newLevelWidthLabel, newLevelHeightLabel, newLevelInfoLabel;
  private Slider newLevelWidthSlider, newLevelHeightSlider;
  private TextButton newLevelCreateButton;

  public EditorLauncherScreen(Betaform betaformRef) {
    this.betaformRef = betaformRef;

    this.create();
  }

  private void initNewLevelTable() {
    this.newLevelTable = new Table(this.skin);
    this.newLevelTable.setFillParent(true);

    /*
     * Layout of the table:
     *
     *              INFORMATION TEXT
     *                        
     * Name field description     |________|
     *
     * |---------------|   Width field description
     *
     * |---------------|   Height field description
     *
     * Saving information          |BUTTON|
     */

    this.newLevelNameField = new TextField("Level name", this.skin);

    int tileWidth = Configuration.Level.TILE_WIDTH;
    int tileHeight = Configuration.Level.TILE_HEIGHT;
    int minHorizontalTiles = Configuration.Video.WINDOW_WIDTH / tileWidth;
    int minVerticalTiles = Configuration.Video.WINDOW_HEIGHT / tileHeight;
    int maxHorizontalTiles = minHorizontalTiles * 100;
    int maxVerticalTiles = minVerticalTiles * 100;

    this.newLevelWidthSlider = new Slider(minHorizontalTiles, maxHorizontalTiles, 1, false, this.skin);
    this.newLevelHeightSlider = new Slider(minVerticalTiles, maxVerticalTiles, 1, false, this.skin);

    this.newLevelLabel = new Label("CREATE A NEW LEVEL OR EDIT AN EXISTING ONE", this.skin);
    this.newLevelNameLabel = new Label("Type the name of an existing level in order to edit it, or a new name to create a new level.", this.skin);
    this.newLevelWidthLabel = new Label("Width (number of tiles)", this.skin);
    this.newLevelHeightLabel = new Label("Height (number of tiles)", this.skin);
    this.newLevelInfoLabel = new Label("Levels are saved in ./core/assets/data/levels/ as name-of-your-level.json files", this.skin);

    this.newLevelCreateButton = new TextButton("Start", this.skin);

    this.newLevelWidthSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        int value = (int) EditorLauncherScreen.this.newLevelWidthSlider.getValue();
        EditorLauncherScreen.this.newLevelWidthLabel.setText("Width (" + value + " tiles)");
      }
    });

    this.newLevelHeightSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        int value = (int) EditorLauncherScreen.this.newLevelHeightSlider.getValue();
        EditorLauncherScreen.this.newLevelHeightLabel.setText("Height (" + value + " tiles)");
      }
    });

    this.newLevelCreateButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        EditorLauncherScreen.this.processNewLevelForm();
      }
    });

    this.newLevelLabel.setAlignment(Align.center, Align.center);
    this.newLevelNameLabel.setAlignment(Align.center, Align.center);
    this.newLevelWidthLabel.setAlignment(Align.center, Align.center);
    this.newLevelHeightLabel.setAlignment(Align.center, Align.center);
    this.newLevelInfoLabel.setAlignment(Align.center, Align.center);

    this.newLevelLabel.setWrap(true);
    this.newLevelNameLabel.setWrap(true);
    this.newLevelWidthLabel.setWrap(true);
    this.newLevelHeightLabel.setWrap(true);
    this.newLevelInfoLabel.setWrap(true);

    // row: form title
    this.newLevelTable.add(this.newLevelLabel)
                      .width(TABLE_CELL_WIDTH * 2)
                      .colspan(2);
    this.newLevelTable.row();

    // row: name label and field
    this.newLevelTable.add(this.newLevelNameLabel)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH);

    this.newLevelTable.add(this.newLevelNameField)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH);

    this.newLevelTable.row();

    // row: width label and field
    this.newLevelTable.add(this.newLevelWidthSlider)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH);

    this.newLevelTable.add(this.newLevelWidthLabel)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH);

    this.newLevelTable.row();

    // row: height label and field
    this.newLevelTable.add(this.newLevelHeightSlider)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH);

    this.newLevelTable.add(this.newLevelHeightLabel)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH);

    this.newLevelTable.row();

    // row: info text and final button
    this.newLevelTable.add(this.newLevelInfoLabel)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH);

    this.newLevelTable.add(this.newLevelCreateButton)
                      .pad(TABLE_CELL_PADDING)
                      .width(TABLE_CELL_WIDTH)
                      .height(TABLE_CELL_WIDTH/4);

    this.stage.addActor(this.newLevelTable);
  }

  private void create() {
    this.stage = new Stage();
    this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

    this.initNewLevelTable();
  }

  @Override
  public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    this.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
    this.stage.draw();
  }

  @Override
  public void resize(int width, int height) {
  }

  @Override
  public void show() {
    /*
     * This EditorScreen object will handle the keyboard events,
     * and the stage will handle the mouse events (e.g. a button clicked).
     */
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    inputMultiplexer.addProcessor(this);
    inputMultiplexer.addProcessor(this.stage);

    Gdx.input.setInputProcessor(inputMultiplexer);
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
    this.stage.dispose();
    this.skin.dispose();
  }

  @Override
  public boolean keyDown(int keycode) {
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    switch(keycode) {
      case Configuration.Controls.KEY_LEAVE:
        this.leave();
        break;

      case Keys.ENTER:
        this.processNewLevelForm();
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

  private void leave() {
    this.betaformRef.mainMenu();
  }

  private void processNewLevelForm() {
    String levelName = this.newLevelNameField.getText();

    Level level;
    
    if(Level.levelExists(levelName)) {
      level = Level.read(levelName);
    } else {
      level = Level.createBaseLevel(
        (int)this.newLevelWidthSlider.getValue(),
        (int)this.newLevelHeightSlider.getValue(),
        this.newLevelNameField.getText()
      );
    }

    this.betaformRef.editor(level);
  }

}

