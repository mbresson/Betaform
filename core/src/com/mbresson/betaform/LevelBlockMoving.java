package com.mbresson.betaform;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class LevelBlockMoving extends LevelBlock implements ObjectMovable, CanHandlePlayerContact {

  public String getType() { return "moving"; } 

  public static class Size {
    private int width, height;

    public Size() {
      width = 1;
      height = 1;
    }

    public Size(Size size) {
      width = size.width;
      height = size.height;
    }
  }

  // size contains the number of horizontal and vertical blocks constituting the final moving block
  // default width: 1, default height: 1
  private Size size = new Size();

  private BehaviorAutomated behavior;

  public LevelBlockMoving() {
    super();
    this.type = "moving";
  }

  public LevelBlockMoving(LevelBlockMoving moving) {
    super(moving);

    this.size = new Size(moving.size);
    this.behavior = moving.behavior;// FIX clone instead of copying the reference
  }

  @Override
  public void update(float deltaTime) {
    super.update(deltaTime);

    this.behavior.behave(this);
  }

  @Override
  public void move(float x, float y) {
    this.body.setLinearVelocity(
      x,
      y
    );
  }

  @Override
  public void changeDirection(Direction direction) {
    // nothing to do
  }

  @Override
  public void stop() {
  }

  /**
   * Shoud be called only once, after loading external resources,
   * because creating the physical body requires to know some information
   * about the graphics.
   */
  @Override
  protected void initPhysics() {
    /**
     * The initPhysics method of LevelBlockMoving differs from the one
     * of the LevelBlock class because a moving block can be constituted of several blocks
     * put together (so the physic shape will be bigger, and the sprite will have to be drawn several times).
     */

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

    float baseWidth = (this.sprite.getWidth()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    float baseHeight = (this.sprite.getHeight()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS;

    float centerX = baseWidth * this.size.width;
    float centerY = baseHeight * this.size.height;

    shape.setAsBox(
      baseWidth * this.size.width,
      baseHeight * this.size.height,
      new Vector2(baseWidth * (this.size.width - 1), baseHeight * (this.size.height-1)),
      0
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.friction = 0f;
    fixtureDef.shape = shape;

    Fixture fixture = this.body.createFixture(fixtureDef);

    shape.dispose();

    this.syncGraphicsFromPhysics();
  }

  @Override
  public void display(SpriteBatch batch) {
    float baseX = this.sprite.getX();
    float baseY = this.sprite.getY();
    float baseWidth = this.sprite.getWidth();
    float baseHeight = this.sprite.getHeight();

    for(int i = 0; i < this.size.width; i++) {
      float x = baseX + i * baseWidth;

      for(int j = 0; j < this.size.height; j++) {
        float y = baseY + j * baseHeight;

        batch.draw(sprite, x, y);
      }
    }
  }

  @Override
  public float getHeight() {
    return super.getHeight() * size.height;
  }

  @Override
  public float getWidth() {
    return super.getWidth() * size.width;
  }

  @Override
  public Vector2 getPhysicsPosition() {
    Vector2 position = new Vector2(
      sprite.getWidth() / Configuration.Physics.WORLD_UNIT_TO_PIXELS * (size.width - 1) / 2f,
      sprite.getHeight() / Configuration.Physics.WORLD_UNIT_TO_PIXELS * (size.height - 1) / 2f
    );

    position.x += super.getPhysicsPosition().x;
    position.y += super.getPhysicsPosition().y;

    return position;
  }

  @Override
  public void handlePlayerContactBegin(Player player, Contact contact) {
    Vector2 playerPos = player.getPhysicsPosition();
    Vector2 blockPos = this.getPhysicsPosition();

    float yRadius = this.getHeight()/2 + player.getHeight()/2 - 0.1f;

    contact.setFriction(0f);
    if((playerPos.y - blockPos.y) >= yRadius) {
      float xRadius = this.getWidth()/2 + player.getWidth();

      if(Math.abs(playerPos.x - blockPos.x) < xRadius) {
        contact.setFriction(1f);
      }
    }
  }

  @Override
  public void handlePlayerContactEnd(Player player, Contact contact) {
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("size", this.size);
    json.writeValue("behavior", this.behavior);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }
}


