package com.mbresson.betaform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.EnumMap;
import java.util.Collection;

public class MainMenuScreen implements Screen, InputProcessor {

  private static enum ButtonType {
    NEW_GAME("New Game"),
    LOAD_GAME("Load Game"),
    EDITOR("Level Editor"),
    QUIT("Quit");

    private String type;

    private ButtonType(String type) {
      this.type = type;
    }

    public String getLabel() {
      return this.type;
    }

    public ButtonType getNext() {
      return values()[ (ordinal()+1) % values().length ];
    }

    public ButtonType getPrevious() {
      int newOrdinal = this.ordinal() - 1;
      if(newOrdinal < 0) {
        newOrdinal = values().length - 1;
      }

      return values()[ newOrdinal ];
    }
  }

  private static final float BUTTON_WIDTH = 300f;
  private static final float BUTTON_HEIGHT = 60f;
  private static final float CURRENT_BUTTON_WIDTH = BUTTON_WIDTH * 1.2f;
  private static final float CURRENT_BUTTON_HEIGHT = BUTTON_HEIGHT * 1.2f;
  private static final float BUTTON_SPACING = CURRENT_BUTTON_HEIGHT / 2;
  private static final int NUM_BUTTONS = ButtonType.values().length;

  private final Betaform betaformRef;

  private SpriteBatch batch;

  private Skin skin;
  private Stage stage;
  private Texture logo;

  private float logoX = 0, logoY = 0;

  private final EnumMap<ButtonType, TextButton> buttons = new EnumMap<>(ButtonType.class);
  private ButtonType currentButtonType = ButtonType.NEW_GAME;

  public MainMenuScreen(Betaform betaformRef) {
    this.betaformRef = betaformRef;

    this.create();
  }

  private void create() {
    this.batch = new SpriteBatch();

    this.stage = new Stage();
    this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

    /*
     * Create the four buttons of the GUI:
     * new game, load game, level editor and quit.
     * The buttons are vertically and horizontally centered.
     */
    float centerY = Gdx.graphics.getHeight() / 2;
    float totalHeight = BUTTON_SPACING * (NUM_BUTTONS - 1) + BUTTON_HEIGHT * NUM_BUTTONS;
    float posY = centerY + totalHeight / 2 - CURRENT_BUTTON_HEIGHT;

    for(ButtonType type: ButtonType.values()) {
      TextButton button = new TextButton(type.getLabel(), skin, "default");

      if(this.currentButtonType == type) {
        button.setWidth(CURRENT_BUTTON_WIDTH);
        button.setHeight(CURRENT_BUTTON_HEIGHT);
      } else {
        button.setWidth(BUTTON_WIDTH);
        button.setHeight(BUTTON_HEIGHT);
      }

      float posX = Gdx.graphics.getWidth() / 2 - button.getWidth() / 2;

      button.setPosition(posX, posY);

      posY -= (BUTTON_SPACING + BUTTON_HEIGHT);

      stage.addActor(button);

      buttons.put(type, button);
    }

    buttons.get(ButtonType.NEW_GAME).addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        MainMenuScreen.this.betaformRef.newGame();
      }
    });

    buttons.get(ButtonType.LOAD_GAME).addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        MainMenuScreen.this.betaformRef.loadGame();
      }
    });

    buttons.get(ButtonType.EDITOR).addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        MainMenuScreen.this.betaformRef.editorLauncher();
      }
    });

    buttons.get(ButtonType.QUIT).addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        MainMenuScreen.this.betaformRef.exit();
      }
    });


    /*
     * Load the logo and position it above the buttons.
     */
    this.logo = new Texture(Gdx.files.internal("img/logo.png"));
    this.logoX = Gdx.graphics.getWidth() / 2 - logo.getWidth() / 2;
    this.logoY = centerY + totalHeight / 2 + logo.getHeight() / 2 + BUTTON_SPACING;
  }

  /**
   * Zooms the new current button and recenters it.
   */
  private void changeCurrentButton(ButtonType newCurrentButtonType) {
    TextButton currentButton = buttons.get(this.currentButtonType);

    currentButton.setWidth(BUTTON_WIDTH);
    currentButton.setHeight(BUTTON_HEIGHT);

    float posX = Gdx.graphics.getWidth() / 2 - BUTTON_WIDTH / 2;
    float posY = currentButton.getY() + (CURRENT_BUTTON_HEIGHT - BUTTON_HEIGHT)/2;
    currentButton.setPosition(posX, posY);

    TextButton newCurrentButton = buttons.get(newCurrentButtonType);

    newCurrentButton.setWidth(CURRENT_BUTTON_WIDTH);
    newCurrentButton.setHeight(CURRENT_BUTTON_HEIGHT);

    float newPosX = Gdx.graphics.getWidth() / 2 - (CURRENT_BUTTON_WIDTH) / 2;
    float newPosY = newCurrentButton.getY() - (CURRENT_BUTTON_HEIGHT - BUTTON_HEIGHT)/2;
    newCurrentButton.setPosition(newPosX, newPosY);

    this.currentButtonType = newCurrentButtonType;
  }

  private void previousButton() {
    this.changeCurrentButton(this.currentButtonType.getPrevious());
  }

  private void nextButton() {
    this.changeCurrentButton(this.currentButtonType.getNext());
  }

  @Override
  public void render(float delta) {
		Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    this.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
    this.stage.draw();

    this.batch.begin();
    this.batch.draw(this.logo, this.logoX, this.logoY);
    this.batch.end();
  }

  @Override
  public void resize(int width, int height) {
  }

  @Override
  public void show() {
    /*
     * This MainMenuScreen object will handle the keyboard events,
     * and the stage will handle the mouse events (e.g. a button clicked).
     */
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    inputMultiplexer.addProcessor(this.stage);
    inputMultiplexer.addProcessor(this);

    Gdx.input.setInputProcessor(inputMultiplexer);

    Gdx.input.setCursorImage(new Pixmap(Gdx.files.internal("img/cursor.png")), 16, 16);
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
    this.batch.dispose();
    this.stage.dispose();
    this.skin.dispose();
    this.logo.dispose();
  }

  @Override
  public boolean keyDown(int keycode) {
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    switch(keycode) {
      case Configuration.Controls.KEY_LEAVE:
        this.betaformRef.exit();
        break;

      case Keys.DOWN:
        this.nextButton();
        break;

      case Keys.UP:
        this.previousButton();
        break;

      case Keys.ENTER:
        this.applyButtonAction(this.currentButtonType);
        break;
    }

    return false;
  }

  private void applyButtonAction(ButtonType buttonType) {
    switch(buttonType) {
      case NEW_GAME:
        this.betaformRef.newGame();
        break;

      case LOAD_GAME:
        this.betaformRef.loadGame();
        break;

      case EDITOR:
        this.betaformRef.editorLauncher();
        break;

      case QUIT:
        this.betaformRef.exit();
        break;
    }
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
}

