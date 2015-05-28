package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * This class is used to represent an abstract kind of object
 * which can be collected by the player (e.g. a health bonus).
 */
abstract class LevelArtifact extends LevelObject {

  private static final String classID = LevelArtifact.class.getName();

  protected String getPath() { return "img/sprites/artifacts.atlas"; }

  protected String type;

  private TextureAtlas textureAtlas;

  /*
   * The player consists of several blocks forming one body.
   * Because of this, several parts of him  may be in contact with the same artifact
   * at the same time.
   * To make sure that the artifact is only used once, we use a boolean.
   */
  private boolean used = false;

  public boolean getUsed() {
    return this.used;
  }

  public LevelArtifact() {
    super();
  }

  public LevelArtifact(LevelArtifact artifact) {
    super(artifact);

    this.textureAtlas = artifact.textureAtlas;
    this.type = new String(artifact.type);
    this.sprite = new Sprite(this.textureAtlas.findRegion(this.getType()));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.initPhysics();
    this.syncGraphicsFromPhysics();
  }

  public void use() {
    this.used = true;
  }

  public String getType() {
    return this.type;
  }

  public void display(SpriteBatch batch) {
    sprite.draw(batch);
  }

  /**
   * Shoud be called only once, after or inside {@link #postloadResources()},
   * because creating the physical body requires to know some information
   * about the graphics.
   */
  private void initPhysics() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;

    bodyDef.position.set(
      this.sprite.getX() / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      this.sprite.getY() / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    this.body = gameRef.getWorld().createBody(bodyDef);
    this.body.setUserData(this);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(
      (this.sprite.getWidth()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      (this.sprite.getHeight()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 1f;
    fixtureDef.isSensor = true; // traversable

    Fixture fixture = body.createFixture(fixtureDef);

    shape.dispose();

    this.syncGraphicsFromPhysics();
  }

  @Override
  public void update(float deltaTime) {
    sprite.setPosition(
      (body.getPosition().x * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getWidth()/2,
      (body.getPosition().y * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getHeight()/2
    );
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
    this.sprite = new Sprite(this.textureAtlas.findRegion(this.getType()));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.initPhysics();
  }

  @Override
  public String getResourceEaterID() { return classID; }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("type", this.type);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }
}

