package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

/**
 * This class is responsible for creating, destroying and displaying bullets.
 * It handles various types of bullets: user-created bullets, enemy bullets.
 */
public class BulletManager implements ResourceEater {

  private static final String classID = BulletManager.class.getName();

  private static final float BULLET_SPEED = 10f;

  public enum BulletType {
    PLAYER_BULLET("player-bullet"),
    PLAYER_SUPER_BULLET("player-super-bullet"),
    ENEMY_BULLET("enemy-bullet");

    private String type;

    private BulletType(String type) {
      this.type = type;
    }

    public String getLabel() {
      return this.type;
    }
  }

  public class Bullet implements Traversable {
    public BulletType type;
    public Body body;
    public Direction direction;
    public int strength = 10;

    public void dispose() {
      GameInstance gameRef = Betaform.getGameRef();

      if(gameRef == null) {
        return;
      }

      gameRef.getWorld().destroyBody(this.body);
    }

    public void invertDirection() {
      this.direction = (this.direction == Direction.LEFT ? Direction.RIGHT : Direction.LEFT);

      this.body.setLinearVelocity(
        direction == Direction.LEFT ? -BULLET_SPEED : BULLET_SPEED,
        0
      );
    }
  }

  private String getPath() { return "img/sprites/bullets.atlas"; }

  private TextureAtlas textureAtlas;
  private Sprite sprite;

  private Array<Bullet> bullets = new Array<>(10);
  private Array<Bullet> bulletsToBeDestroyed = new Array<>(10);

  public BulletManager() {
  }

  public void newBullet(BulletType type, Vector2 physicsPosition, Direction direction, int strength) {
    sprite.setRegion(textureAtlas.findRegion(type.getLabel()));

    Bullet bullet = new Bullet();
    bullet.type = type;
    bullet.strength = strength;
    bullet.direction = direction;

    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;

    float finalX = physicsPosition.x;
    if(direction == Direction.LEFT) {
      finalX -= sprite.getWidth()/2 / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    } else {
      finalX += sprite.getWidth()/2 / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    }
    float finalY = physicsPosition.y + sprite.getHeight() / Configuration.Physics.WORLD_UNIT_TO_PIXELS;

    bodyDef.position.set(
      finalX,
      finalY
    );

    GameInstance gameRef = Betaform.getGameRef();

    if(gameRef == null) {
      return;
    }

    bullet.body = gameRef.getWorld().createBody(bodyDef);
    bullet.body.setUserData(bullet);
    bullet.body.setBullet(true);
    bullet.body.setGravityScale(0);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(
      sprite.getWidth()/2 / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      sprite.getHeight()/2 / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 0f;
    fixtureDef.friction = 0f;
    fixtureDef.isSensor = true;

    Fixture fixture = bullet.body.createFixture(fixtureDef);
    shape.dispose();

    bullet.body.setLinearVelocity(
      direction == Direction.LEFT ? -BULLET_SPEED : BULLET_SPEED,
      0
    );

    bullets.add(bullet);
  }

  /**
   * Display all existing bullets.
   */
  public void display(SpriteBatch batch) {
    for(Bullet bullet: this.bullets) {
      sprite.setRegion(textureAtlas.findRegion(bullet.type.getLabel()));

      float x = bullet.body.getPosition().x * Configuration.Physics.WORLD_UNIT_TO_PIXELS - sprite.getWidth() / 2;
      float y = bullet.body.getPosition().y * Configuration.Physics.WORLD_UNIT_TO_PIXELS - sprite.getHeight() / 2;

      if(bullet.direction == Direction.LEFT) {
        sprite.flip(true, false);
        batch.draw(sprite, x, y);
        sprite.flip(true, false);
      } else {
        batch.draw(sprite, x, y);
      }
    }
  }

  // deltaTime is a placeholder
  public void update(float deltaTime) {
    for(Bullet bullet: bulletsToBeDestroyed) {
      this.bullets.removeValue(bullet, true);
      bullet.dispose();
    }

    bulletsToBeDestroyed.clear();
  }

  public void destroyBullet(Bullet bullet) {
    /*
     * We queue the bullets to be destroyed:
     * we can't destroy them while the physics engine is in the middle of its computations.
     */

    /*
     * Also, make sure a bullet is not set to be destroyed twice:
     * it can happen if several fixtures of the player are in contact with the bullet at the same time
     * and call the destroyBullet function at the same time.
     */
    if(this.bulletsToBeDestroyed.contains(bullet, true)) {
      return;
    }

    bulletsToBeDestroyed.add(bullet);
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

    this.sprite = new Sprite(this.textureAtlas.findRegion(BulletType.PLAYER_BULLET.getLabel()));
  }

  @Override
  public String getResourceEaterID() { return classID; }
}

