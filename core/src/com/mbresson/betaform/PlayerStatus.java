package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

/**
 * This class is responsible for displaying the player's status
 * (health, score, etc) on the screen.
 */
public class PlayerStatus implements ResourceEater {

  private static final String classID = PlayerStatus.class.getName();

  protected String getPath() { return "img/sprites/status.atlas"; }

  private static final String FONT_PATH = "fonts/DejaVuSans.ttf";
  private static final int FONT_SIZE = 28;

  private static final String STATUS_HEALTH = "health";
  private static final String STATUS_SCORE = "score";
  private static final String STATUS_BATTERY = "battery";
  private static final String STATUS_SHIELD = "shield";
  private static final String STATUS_SPEED = "speed";
  private static final String STATUS_STRENGTH = "strength";

  private static final int OVERLAY_PADDING = 5;
  private static final int ADDON_OFFSET = 10;

  private int overlayX, overlayY;
  private int statusY, healthX, scoreX, batteryX;
  private int healthBarX, batteryBarX, scoreTextX, scoreTextY;
  private int statusBarY, statusBarHeight, statusBarMaxWidth;
  private int totalWidth, totalHeight;

  private Overlay overlay = new Overlay();

  private TextureAtlas textureAtlas;
  private Sprite sprite;
  private BitmapFont font;

  private void displayProgressBar(SpriteBatch batch, int percentage, int x, int y, int max_width, int height, Overlay.Color color) {
    overlay.setAlpha(0.8f);
    overlay.setColor(color);

    overlay.display(batch, x, y, max_width * percentage/100, height);
    overlay.setAlpha(1f);
    overlay.setColor(Overlay.Color.BLACK);
  }

  private void displayProgressBar(SpriteBatch batch, int percentage, int x, int y, int max_width, int height) {
    displayProgressBar(batch, percentage, x, y, max_width, height, Overlay.Color.BLACK);
  }

  /**
   * Status display steps:
   * 1 Draw health, score and battery information.
   *   The battery displayed is the battery of the addon currently in use.
   *   If no addon is used, the player's inner battery is displayed.
   *
   * 2 Draw the player's addons.
   *
   * @param health a percentage (min: 0, max: 100)
   * @param battery a percentage (min: 0, max: 100)
   */
  public void display(SpriteBatch batch, int health, int score, int battery, Array<PlayerAddon> addons, int currentAddon) {
    /*
     * 1 Draw health, score and battery information.
     */

    // draw a semi-transparent black overlay.
    overlay.setAlpha(0.5f);
    overlay.setColor(Overlay.Color.BLACK);
    overlay.display(batch, overlayX, overlayY, totalWidth + OVERLAY_PADDING * 2, totalHeight + OVERLAY_PADDING * 2);
    overlay.setAlpha(1f);

    // draw the health
    sprite.setPosition(healthX, statusY);
    sprite.setRegion(textureAtlas.findRegion(STATUS_HEALTH));
    sprite.setSize(
      sprite.getRegionWidth(),
      sprite.getRegionHeight()
    );
    sprite.draw(batch);

    if(health < 10) {
      this.displayProgressBar(batch, health, healthBarX, statusBarY, statusBarMaxWidth, statusBarHeight, Overlay.Color.RED);
    } else {
      this.displayProgressBar(batch, health, healthBarX, statusBarY, statusBarMaxWidth, statusBarHeight);
    }


    // draw the score
    sprite.setPosition(scoreX, statusY);
    sprite.setRegion(textureAtlas.findRegion(STATUS_SCORE));
    sprite.setSize(
      sprite.getRegionWidth(),
      sprite.getRegionHeight()
    );
    sprite.draw(batch);

    font.draw(batch, Integer.toString(score), scoreTextX, scoreTextY);

    // draw the battery
    sprite.setPosition(batteryX, statusY);
    sprite.setRegion(textureAtlas.findRegion(STATUS_BATTERY));
    sprite.draw(batch);

    /*
     * Draw the player's inner battery.
     *
     * If the player is using an addon, draw its battery right of the player's inner battery as well.
     */
    Overlay.Color batteryColor = Overlay.Color.BLACK;
    if(battery < 25) {
      batteryColor = Overlay.Color.RED;
    }

    if(currentAddon > -1) {
      PlayerAddon addon = addons.get(currentAddon);

      int addonBattery = addon.getBattery() * 100 / PlayerAddon.MAX_BATTERY;

      Overlay.Color addonBatteryColor = Overlay.Color.BLACK;
      if(addonBattery < 25) {
        addonBatteryColor = Overlay.Color.RED;
      } else {
        switch(addon) {
          case ADDON_SHIELD:
            addonBatteryColor = Overlay.Color.PURPLE;
            break;

          case ADDON_SPEED:
            addonBatteryColor = Overlay.Color.BLUE;
            break;

          case ADDON_STRENGTH:
            addonBatteryColor = Overlay.Color.ORANGE;
            break;
        }
      }

      this.displayProgressBar(batch, battery, batteryBarX, statusBarY, statusBarMaxWidth/2, statusBarHeight, batteryColor);
      this.displayProgressBar(batch, addonBattery, batteryBarX + statusBarMaxWidth/2, statusBarY, statusBarMaxWidth/2, statusBarHeight, addonBatteryColor);
    } else {
      this.displayProgressBar(batch, battery, batteryBarX, statusBarY, statusBarMaxWidth, statusBarHeight, batteryColor);
    }

    /*
     * 2 Draw each addon owned by the player.
     * All addons are displayed with opacity = 50%, except for the addon currently in use.
     */
    sprite.setPosition(ADDON_OFFSET, ADDON_OFFSET); // initial coordinates: at the bottom left corner
    for(int addonIndex = 0, size = addons.size; addonIndex < addons.size; addonIndex++) {
      if(currentAddon == addonIndex) {
        sprite.setAlpha(1f);
      } else {
        sprite.setAlpha(0.5f);
      }

      sprite.setRegion(textureAtlas.findRegion(addons.get(addonIndex).getLabel()));

      sprite.setSize(
        sprite.getRegionWidth(),
        sprite.getRegionHeight()
      );

      sprite.draw(batch);

      sprite.setPosition(
        sprite.getX() + sprite.getWidth() + ADDON_OFFSET,
        sprite.getY()
      );

      sprite.setAlpha(1f);

    }
  }

  /**
   * Should be called in postloadResources() and only once.
   */
  private void initGeometry() {
    int spriteHeight = (int)sprite.getHeight(), spriteWidth = (int)sprite.getWidth();

    // draw the status pane at the top of the window, with some space above
    statusY = Gdx.graphics.getHeight() - (spriteHeight + OVERLAY_PADDING*2);

    /*
     * The status is displayed like so: health, score and then the battery power (3 parts).
     * We give each part enough width to display its corresponding icon 4 times.
     * This way, we can be sure that there is enough space for progress bars and texts.
     *
     * The status is centered at the top of the window.
     */
    int partWidth = spriteWidth * 4;
    totalWidth = partWidth * 3;
    totalHeight = spriteHeight;
    int centerX = Gdx.graphics.getWidth()/2;

    healthX = centerX - totalWidth/2;
    healthBarX = healthX + spriteWidth + spriteWidth/4;

    scoreX = healthX + partWidth;
    scoreTextX = scoreX + spriteWidth + spriteWidth/4;
    scoreTextY = statusY + spriteHeight/2 + (int)font.getCapHeight()/2;

    batteryX = scoreX + partWidth;
    batteryBarX = batteryX + spriteWidth + spriteWidth/4;

    overlayX = healthX - OVERLAY_PADDING;
    overlayY = statusY - OVERLAY_PADDING;

    statusBarY = statusY + spriteHeight/4;
    statusBarHeight = spriteHeight/2;
    statusBarMaxWidth = partWidth - spriteWidth*2;
  }

  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
    ResourceLoader loader = ResourceLoader.getInstance();

    if(!loader.isPreloaded(this.getResourceEaterID())) {
      AssetManager manager = ResourceLoader.getInstance().registerForPreloading(this.getResourceEaterID());

      /*
       * Load the TTF font file.
       */
      FileHandleResolver resolver = new InternalFileHandleResolver();
      manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
      manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

      FreeTypeFontLoaderParameter fontParameter = new FreeTypeFontLoaderParameter();
      fontParameter.fontFileName = FONT_PATH;
      fontParameter.fontParameters.size = FONT_SIZE;

      manager.load("font-" + FONT_SIZE + ".ttf", BitmapFont.class, fontParameter);

      /*
       * Load the texture atlas.
       */
      manager.load(this.getPath(), TextureAtlas.class);
    }

    this.overlay.preloadResources();
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    AssetManager manager = ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.overlay.postloadResources();

    this.font = manager.get("font-" + FONT_SIZE + ".ttf", BitmapFont.class);

    this.textureAtlas = manager.get(this.getPath(), TextureAtlas.class);

    this.sprite = new Sprite(this.textureAtlas.findRegion(STATUS_HEALTH));

    this.initGeometry();
  }

  @Override
  public String getResourceEaterID() { return classID; }
}

