package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class LevelTile implements CanBeHitByBullet, Json.Serializable, Editable {

  private Body body;
  private int x, y, width = 1, height = 1;

  public int getNumberOfHorizontalBlocks() {
    return width;
  }

  public int getNumberOfVerticalBlocks() {
    return height;
  }

  public static enum TileType {
    NORMAL("normal"),
    BOUNCY("bouncy");

    private String type;

    private TileType(String type) {
      this.type = type;
    }

    public String getLabel() {
      return this.type;
    }

    public static TileType findByName(String name) {
      for(TileType type: values()) {
        if(type.type.equals(name)) {
          return type;
        }
      }

      throw new IllegalArgumentException("No type for the Tile named " + name);
    }
  }

  public LevelTile() {
  }

  public LevelTile(TileType type) {
    this();
    this.type = type.getLabel();
  }

  public LevelTile(int x, int y, int width, int height) {
    this(x, y, width, height, TileType.NORMAL);
  }

  public LevelTile(int x, int y, int width, int height, TileType type) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.type = type.getLabel();
  }

  public LevelTile(LevelTile tile) {
    this.x = tile.x;
    this.y = tile.y;
    this.width = tile.width;
    this.height = tile.height;
    this.type = tile.type;

    this.load(tile.getSpriteSize());
  }

  private Vector2 getSpriteSize() {
    return this.spriteSize;
  }

  private String type = TileType.NORMAL.getLabel();
  private Vector2 spriteSize = new Vector2();

  public TileType getType() {
    return TileType.findByName(this.type);
  }

  public Vector2 getPhysicsPosition() {
    return body.getPosition();
  }

  /**
   * Since, for the tiles, physics and graphics are decoupled,
   * we need to pass the size of the graphics object to this method
   * to compute its physical size.
   */
  public void load(Vector2 spriteSize) {
    this.spriteSize = new Vector2(spriteSize);

    float spriteWidth = spriteSize.x;
    float spriteHeight = spriteSize.y;

    float posX = x + spriteWidth/2;
    float posY = y + spriteHeight/2;

    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;

    bodyDef.position.set(
      posX / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      posY / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    body = gameRef.getWorld().createBody(bodyDef);
    body.setUserData(this);

    float baseWidth = (spriteWidth/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    float baseHeight = (spriteHeight/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS;

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(
      baseWidth * width,
      baseHeight * height,
      new Vector2(baseWidth*(width-1), baseHeight*(height-1)),
      0
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 1f;
    fixtureDef.friction = 0.1f;

    Fixture fixture = body.createFixture(fixtureDef);
    fixture.setUserData(this);

    shape.dispose();
  }

  /**
   * @return always true because a tile cannot be dead
   */
  @Override
  public boolean hitBy(BulletManager.Bullet bullet) {
    // bouncy tiles make the bullet change direction
    if(this.type.equals(TileType.BOUNCY.getLabel())) {
      bullet.invertDirection();
    } else {
      GameInstance gameRef = Betaform.getGameRef();
      if(gameRef == null) {
        return true;
      }

      gameRef.getBulletManager().destroyBullet(bullet);
    }

    return true;
  }

  @Override
  public void setEditablePosition(int x, int y) {

    if(this.body == null) {
      return;
    }

    this.x = x;
    this.y = y;

    this.body.setTransform(
      x / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      y / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      0
    );
  }

  @Override
  public void write(Json json) {
    json.writeValue("x", this.x);
    json.writeValue("y", this.y);
    json.writeValue("width", this.width);
    json.writeValue("height", this.height);
    json.writeValue("type", this.type);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }

}

