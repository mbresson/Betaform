package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

abstract class LevelEnemy extends LevelObject implements ObjectMovable, CanBeHitByBullet, CanHandlePlayerContact {

  private static final String classID = LevelEnemy.class.getName();

  protected int getMaxHealth() { return 100; }

  protected int health = this.getMaxHealth();
  protected boolean moving = false;

  protected Behavior behavior;
  protected Direction direction = Direction.LEFT;

  private TextureAtlas textureAtlas;

  protected int getFirstRegionIndex() { return 1; }
  protected int getLastRegionIndex() { return 1; }
  protected float getAnimationStep() { return 1.0f / 5f; }
  protected BodyDef.BodyType getBodyType() { return BodyDef.BodyType.DynamicBody; }

  private int regionIndex = getFirstRegionIndex();
  private float regionTime = 0f;

  public int getStrength() { return 10; }
  
  public Direction getDirection() {
    return this.direction;
  }

  /**
   * Shoud be called only once, after or inside {@link #postloadResources()},
   * because creating the physical body requires to know some information
   * about the graphics.
   */
  private void initPhysics() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = this.getBodyType();
    bodyDef.fixedRotation = true;

    bodyDef.position.set(
      (sprite.getX() + sprite.getWidth() / 2f) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      (sprite.getY() + sprite.getHeight() / 2f) / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    this.body = gameRef.getWorld().createBody(bodyDef);
    this.body.setUserData(this);

    float physicsWidth = this.sprite.getWidth() / Configuration.Physics.WORLD_UNIT_TO_PIXELS / 2;
    float physicsHeight = this.sprite.getHeight() / Configuration.Physics.WORLD_UNIT_TO_PIXELS / 2;

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(physicsWidth, physicsHeight);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 1f;
    fixtureDef.friction = 0f;
    fixtureDef.restitution = 0f;
    this.body.setGravityScale(10f);

    Fixture fixture = this.body.createFixture(fixtureDef);
    shape.dispose();

    this.syncGraphicsFromPhysics();
  }

  public void display(SpriteBatch batch) {
    if(direction == Direction.LEFT) {
      sprite.flip(true, false);
      sprite.draw(batch);
      sprite.flip(true, false);
    } else {
      sprite.draw(batch);
    }
  }

  public void update(float deltaTime) {
    sprite.setPosition(
      (body.getPosition().x * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getWidth()/2,
      (body.getPosition().y * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getHeight()/2
    );

    if(moving) {
      regionTime += deltaTime;
      if(regionTime > this.getAnimationStep()) {
        regionIndex++;

        if(regionIndex > this.getLastRegionIndex()) {
          regionIndex = this.getFirstRegionIndex();
        }

        sprite.setRegion(textureAtlas.findRegion(Integer.toString(regionIndex)));

        regionTime = 0f;
      }
    }

    this.behavior.behave(this);
  }

  @Override
  public void move(float x, float y) {
    this.body.setLinearVelocity(
      x,
      y
    );

    this.direction = x < 0 ? Direction.LEFT : Direction.RIGHT;

    this.moving = true;
  }

  @Override
  public void stop() {
    this.moving = false;
    resetAnimation();
  }

  private void resetAnimation() {
    regionIndex = this.getFirstRegionIndex();
    regionTime = 0f;

    sprite.setRegion(textureAtlas.findRegion(Integer.toString(regionIndex)));
  }

  @Override
  public void changeDirection(Direction direction) {
    // nothing to do
  }

  @Override
  public boolean hitBy(BulletManager.Bullet bullet) {
    this.health -= bullet.strength;

    float yImpulse = this.getHeight()*2;
    float xImpulse = this.getWidth()*2;

    if(bullet.direction == Direction.LEFT) {
      xImpulse = -xImpulse;
    }

    this.body.applyLinearImpulse(
      new Vector2(xImpulse, yImpulse),
      this.body.getWorldCenter(),
      true
    );

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return true;
    }

    gameRef.getBulletManager().destroyBullet(bullet);

    if(this.health <= 0) {
      gameRef.getLevel().removeObject(this);
      return false;
    }

    return true;
  }

  public Direction getDirectionToPlayer(Player player) {
    float playerX = player.getPhysicsPosition().x;
    float enemyX = this.body.getPosition().x;

    return playerX < enemyX ? Direction.LEFT : Direction.RIGHT;
  }

  @Override
  public void handlePlayerContactBegin(Player player, Contact contact) {
    player.hitBy(this);
  }

  @Override
  public void handlePlayerContactEnd(Player player, Contact contact) {
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
    this.sprite = new Sprite(this.textureAtlas.findRegion(Integer.toString(this.getFirstRegionIndex())));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.initPhysics();
  }

  @Override
  public String getResourceEaterID() { return classID; }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("behavior", this.behavior);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }
}

