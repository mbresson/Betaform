package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * A hint is an object displaying help text
 * when the user presses the action key on it.
 */
public class LevelHint extends LevelHelper implements LevelInteractable {

  private static final String classID = LevelHint.class.getName();

  private static final String FONT_PATH = "fonts/DejaVuSans.ttf";
  private static final int FONT_SIZE = 22;
  private static final int MAX_TEXT_WIDTH = Configuration.Video.WINDOW_WIDTH / 3;
  private static final int OVERLAY_PADDING = Configuration.Video.WINDOW_WIDTH/20;

  private Array<String> text = new Array<>();
  private int textIndex = -1;

  private boolean interacting = false;

  @Override
  public String getType() { return "hint"; }

  private Overlay overlay = new Overlay();
  private BitmapFont font;
  private Array<GlyphLayout> textLayout = new Array<>();

  public LevelHint() {
  }

  public LevelHint(LevelHint hint) {
    super(hint);
  }

  public void displayInteraction(SpriteBatch batch) {
    if(textIndex == text.size) {
      return;
    }

    GlyphLayout layout = this.textLayout.get(textIndex);

    float textX = Configuration.Video.WINDOW_WIDTH/2 - layout.width/2;
    float textY = Configuration.Video.WINDOW_HEIGHT/2 + layout.height/2;

    /*
     * We draw the overlay to fit the text that will be displayed later.
     */
    float overlayWidth = layout.width + OVERLAY_PADDING*2, overlayHeight = layout.height + OVERLAY_PADDING*2;
    float overlayX = Configuration.Video.WINDOW_WIDTH/2 - overlayWidth/2, overlayY = Configuration.Video.WINDOW_HEIGHT/2 - overlayHeight/2;

    this.overlay.display(
      batch,
      overlayX,
      overlayY,
      overlayWidth,
      overlayHeight
    );

    /*
     * We draw the text at the center of the screen.
     */
    font.draw(
      batch,
      layout,
      textX,
      textY
    );
  }

  public Array<String> getText() {
    return this.text;
  }

  public void reset() {
    /*
     * We start at -1 because the index is incremented at the start of every interaction.
     * If we set the index to 0 initially, it will be set to 1 after the first interaction
     * (when the user first presses the action key to see the help text)
     * and so the displayInteraction function will never show the first help string (of index 0) but only the second.
     */
    this.textIndex = -1;

    this.interacting = false;
  }

  @Override
  public boolean interact() {
    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return true;
    }

    gameRef.setPaused(true);
    textIndex++;

    if(textIndex == text.size) {
      this.reset();
      gameRef.setPaused(false);
      return true;
    }

    this.interacting = true;

    return false;
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
    ResourceLoader loader = ResourceLoader.getInstance();

    if(!loader.isPreloaded(this.getResourceEaterID())) {
      AssetManager manager = ResourceLoader.getInstance().registerForPreloading(this.getResourceEaterID());

      FileHandleResolver resolver = new InternalFileHandleResolver();
      manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
      manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

      FreeTypeFontLoaderParameter fontParameter = new FreeTypeFontLoaderParameter();
      fontParameter.fontFileName = FONT_PATH;
      fontParameter.fontParameters.size = FONT_SIZE;

      manager.load("font-" + FONT_SIZE + ".ttf", BitmapFont.class, fontParameter);

      manager.load(this.getPath(), TextureAtlas.class);
    }

    this.overlay.preloadResources();
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    this.overlay.postloadResources();
    this.overlay.setAlpha(0.8f);
    this.overlay.setColor(Overlay.Color.GREY);

    AssetManager manager = ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.textureAtlas = manager.get(this.getPath(), TextureAtlas.class);
    this.sprite = new Sprite(this.textureAtlas.findRegion(this.getType()));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    super.initPhysics();

    this.font = manager.get("font-" + FONT_SIZE + ".ttf", BitmapFont.class);

    for(String currentText: this.text) {
      GlyphLayout layout = new GlyphLayout();

      layout.setText(
        this.font,
        currentText,
        Color.WHITE,
        MAX_TEXT_WIDTH,
        Align.left,
        true
      );

      this.textLayout.add(layout);
    }
  }

  @Override
  public String getResourceEaterID() { return classID; }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("text", this.text);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }
}

