package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class LevelDoor extends LevelObject {

  private static final String classID = LevelDoor.class.getName();

  public enum DoorState {
    CLOSED("closed"),
    OPEN("open");

    private String type;

    private DoorState(String type) {
      this.type = type;
    }

    public String getLabel() {
      return this.type;
    }
  }

  protected String getPath() { return "img/sprites/door.atlas"; }
  protected DoorState state = DoorState.CLOSED;

  private int maxOpeners = 0;
  private int numOpenersOn = 0; // when numOpenersOn == maxOpeners, the door will be opened

  // a unique identifier for each door
  private int number;

  protected TextureAtlas textureAtlas;
  protected Fixture bodyFixture;

  public LevelDoor() {
    super();
  }

  public LevelDoor(LevelDoor door) {
    super(door);

    this.number = door.number;
    this.state = door.state;

    this.textureAtlas = door.textureAtlas;
    this.sprite = new Sprite(this.textureAtlas.findRegion(this.state.getLabel()));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.initPhysics();
    this.syncGraphicsFromPhysics();
  }

  public void display(SpriteBatch batch) {
    sprite.draw(batch);
  }

  /**
   * Shoud be called only once, after or inside {@link #postloadResources()},
   * because creating the physical body requires to know some information
   * about the graphics.
   */
  public void initPhysics() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.KinematicBody;

    bodyDef.position.set(
      sprite.getX() / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      sprite.getY() / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    body = gameRef.getWorld().createBody(bodyDef);
    body.setUserData(this);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(
      (sprite.getWidth()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      (sprite.getHeight()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;

    bodyFixture = body.createFixture(fixtureDef);

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

  public void setState(DoorState state) {
    sprite.setRegion(textureAtlas.findRegion(state.getLabel()));
    this.state = state;
  }

  /**
   * Increase the number of switches needed to open this door.
   * Ideally, this number should not be modified in game
   * and only called when loading the level.
   */
  public void increaseMaxOpeners() {
    this.maxOpeners++;
  }

  public void increaseNumOpenersOn() {
    this.numOpenersOn++;
    if(this.numOpenersOn == this.maxOpeners) {
      this.open();
    }
  }

  public void decreaseNumOpenersOn() {
    this.numOpenersOn--;
    if(this.numOpenersOn == this.maxOpeners - 1) {
      this.close();
    }
  }

  private void open() {
    this.state = DoorState.OPEN;
    this.sprite.setRegion(textureAtlas.findRegion(this.state.getLabel()));

    this.bodyFixture.setSensor(true);
  }

  private void close() {
    this.state = DoorState.CLOSED;
    this.sprite.setRegion(textureAtlas.findRegion(this.state.getLabel()));

    this.bodyFixture.setSensor(false);
  }

  public int getNumber() {
    return this.number;
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

    this.sprite = new Sprite(this.textureAtlas.findRegion(this.state.getLabel()));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.initPhysics();
  }

  @Override
  public String getResourceEaterID() { return classID; }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("number", this.number);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }
}

