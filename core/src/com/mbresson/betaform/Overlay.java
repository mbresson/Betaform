package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Used to draw some overlay with a dark background color.
 */
public class Overlay implements ResourceEater {

  private static final String classID = Overlay.class.getName();

  protected String getPath() { return "img/sprites/overlay.atlas"; }

  public enum Color {
    BLACK("black"),
    BLUE("blue"),
    GREY("grey"),
    ORANGE("orange"),
    PURPLE("purple"),
    RED("red");

    private String type;

    private Color(String type) {
      this.type = type;
    }

    public String getLabel() {
      return type;
    }
  }

  private TextureAtlas textureAtlas;
  private Sprite sprite;

  public void setColor(Color color) {
    sprite.setRegion(textureAtlas.findRegion(color.getLabel()));
  }

  public void setAlpha(float alpha) {
    sprite.setAlpha(alpha);
  }

  public void display(SpriteBatch batch, float x, float y, float width, float height) {
    sprite.setPosition(x, y);
    sprite.setSize(width, height);

    sprite.draw(batch);
  }

  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
    ResourceLoader loader = ResourceLoader.getInstance();

    if(!loader.isPreloaded(this.getResourceEaterID())) {
      AssetManager manager = ResourceLoader.getInstance().registerForPreloading(this.getResourceEaterID());

      manager.load(this.getPath(), TextureAtlas.class);
    }
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    AssetManager manager = ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.textureAtlas = manager.get(this.getPath(), TextureAtlas.class);
    this.sprite = new Sprite(this.textureAtlas.findRegion(Color.BLACK.getLabel()));
  }

  @Override
  public String getResourceEaterID() { return classID; }
}

