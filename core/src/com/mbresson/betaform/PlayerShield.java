package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerShield implements ResourceEater {

  private static final String classID = PlayerShield.class.getName();

  protected String getPath() { return "img/sprites/shield.atlas"; }

  private TextureAtlas textureAtlas;
  private Sprite sprite;

  private float width, height;

  public void display(SpriteBatch batch, float centerX, float centerY) {
    sprite.setPosition(
      centerX - width/2,
      centerY - height/2
    );

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
    this.sprite = new Sprite(this.textureAtlas.findRegion("shield"));

    this.width = this.sprite.getWidth();
    this.height = this.sprite.getHeight();
  }

  @Override
  public String getResourceEaterID() { return classID; }
}

