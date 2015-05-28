package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * This class is used to draw tiles on the screen,
 * so that we have only 1 sprite and texture atlas for as many LevelTile as we want.
 */
public class LevelTileDisplayer implements ResourceEater {

  private static final String classID = LevelTileDisplayer.class.getName();

  private String getPath() { return "img/sprites/tiles.atlas"; }

  private TextureAtlas textureAtlas;
  private Sprite sprite;

  public Vector2 getSpriteSizeForTile(LevelTile.TileType type) {
    TextureRegion region = textureAtlas.findRegion(type.getLabel());
    return new Vector2(
      region.getRegionWidth(),
      region.getRegionHeight()
    );
  }

  public void display(SpriteBatch batch, LevelTile tile) {
    sprite.setRegion(textureAtlas.findRegion(tile.getType().getLabel()));

    Vector2 physicsPosition = tile.getPhysicsPosition();

    float baseX = physicsPosition.x * Configuration.Physics.WORLD_UNIT_TO_PIXELS - sprite.getWidth()/2;
    float baseY = physicsPosition.y * Configuration.Physics.WORLD_UNIT_TO_PIXELS - sprite.getHeight()/2;
    float baseWidth = sprite.getWidth();
    float baseHeight = sprite.getHeight();
    int horizontalBlocks = tile.getNumberOfHorizontalBlocks();
    int verticalBlocks = tile.getNumberOfVerticalBlocks();

    for(int i = 0; i < horizontalBlocks; i++) {
      float x = baseX + i*baseWidth;

      for(int j = 0; j < verticalBlocks; j++) {
        float y = baseY + j*baseHeight;

        batch.draw(sprite, x, y);
      }
    }

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
    this.sprite = new Sprite(this.textureAtlas.findRegion(LevelTile.TileType.NORMAL.getLabel()));
  }

  @Override
  public String getResourceEaterID() { return classID; }
}

