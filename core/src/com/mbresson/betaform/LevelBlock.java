package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.*;

abstract class LevelBlock extends LevelObject {

  private static final String classID = LevelBlock.class.getName();

  protected String getPath() { return "img/sprites/blocks.atlas"; }
  protected String type;

  protected TextureAtlas textureAtlas;

  public String getType() {
    return this.type;
  }

  public LevelBlock() {
    super();
  }

  public LevelBlock(LevelBlock block) {
    super(block);

    this.type = block.getType();
    this.sprite = new Sprite(this.textureAtlas.findRegion(this.getType()));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.initPhysics();
  }

  public void display(SpriteBatch batch) {
    sprite.draw(batch);
  }

  /**
   * Shoud be called only once, after or inside {@link #postloadResources()},
   * because creating the physical body requires to know some information
   * about the graphics.
   */
  protected void initPhysics() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.KinematicBody;

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
      (sprite.getWidth()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      (sprite.getHeight()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;

    Fixture fixture = this.body.createFixture(fixtureDef);

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
}


