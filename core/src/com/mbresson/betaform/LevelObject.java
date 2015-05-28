package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Represents any kind of displayable object
 * which the player can interact with (even the player itself).
 */
public class LevelObject implements ResourceEater, Json.Serializable, Editable {

  private static final String classID = LevelObject.class.getName();

  /**
   * This is the initial position of the object, read from the level file.
   * When the game is running, the current position will be stored and managed
   * by the physics engine.
   */
  protected Vector2D position = new Vector2D(0, 0);

  protected Body body;
  protected Sprite sprite;

  protected String getPath() {
    return "";
  }

  public Vector2D getPosition() {
    return this.position;
  }

  @Override
  public void setEditablePosition(int x, int y) {

    this.position = new Vector2D(x, y);

    this.body.setTransform(
      x / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      y / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      0
    );

    this.syncGraphicsFromPhysics();
  }

  public LevelObject() {
  }

  public LevelObject(LevelObject object) {
    this.position = new Vector2D(object.position);
  }

  public void setPosition(Vector2D position) {
    this.position = position;

    if(this.body == null) {
      return;
    }

    this.body.getPosition().x = position.getX() / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    this.body.getPosition().y = position.getY() / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
  }

  public float getWidth() {
    return this.sprite.getWidth() / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
  }

  public float getHeight() {
    return this.sprite.getHeight() / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
  }

  public float getDistanceToObject(LevelObject object) {
    return this.body.getPosition().dst(object.body.getPosition());
  }

  /**
   * Must be called at each frame
   */
  public void display(SpriteBatch batch) {
    // nothing!
  }

  public void update(float deltaTime) {
  }

  public void dispose() {
    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    gameRef.getWorld().destroyBody(this.body);
  }

  public Vector2 getPhysicsPosition() {
    return this.body.getPosition();
  }

  /**
   * Each extending class will have to implements this method itself
   * since LevelObject doesn't hold much information about graphics itself.
   */
  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
  }

  /**
   * See {@link #preloadResources()}
   */
  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
  }

  @Override
  public String getResourceEaterID() { return classID; }

  @Override
  public void write(Json json) {
    json.writeValue("position", this.position);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }

  /**
   * Updates the sprite's coordinates according to the physical body's coordinates.
   */
  protected void syncGraphicsFromPhysics() {
    sprite.setPosition(
      (body.getPosition().x * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getWidth()/2,
      (body.getPosition().y * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getHeight()/2
    );
  }
}

