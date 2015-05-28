package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Player extends LevelObject implements CanBeHitByBullet {

  private static final String classID = Player.class.getName();

  public static enum BodyPart {
    FEET,
    MIDDLE;

    private static final int FEET_WIDTH = 8; // TODO clean up
  }

  protected String getPath() { return "img/sprites/player.atlas"; }

  private static final int FIRST_REGION = 1, LAST_REGION = 8;
  private static final float ANIMATION_STEP = 1.0f / 5f;
  private static final float FRICTION = 1.0f;

  public static final int MAX_HEALTH = 100;
  public static final int MAX_BATTERY = 1000;

  private static final float SPEED = 3.9f;
  private static final int STRENGTH = 10;

  // the amount of power needed to shoot and jump
  private static final int SHOOTING_POWER_USE = MAX_BATTERY / 10;
  private static final int JUMPING_POWER_USE = MAX_BATTERY / 15;
  private static final int MOVING_POWER_USE = 1;

  private TextureAtlas textureAtlas;

  private int regionIndex = FIRST_REGION;
  private float regionTime = 0f;

  private boolean resetPosition = false;

  private Direction direction = Direction.LEFT;
  private boolean moving = false, pushing = false;
  private int partsInsideExitPortal = 0; // see isReadyToExit() method

  private int health = Player.MAX_HEALTH;
  private int battery = Player.MAX_BATTERY;
  private int score = 0;

  /**
   * The player can only own one addon of the same type.
   */

  private Array<PlayerAddon> addons = new Array<>(3);
  private Array<LevelInteractable> interactables = new Array<>(5);
  private LevelInteractable nearestInteractable = null;
  private boolean interacting = false;

  // if the player's feet are standing on minimum 1 object, the player can jump
  private int objectsBelowFeet = 0;

  private Array<LevelBlockMovable> movables = new Array<>(2);

  // -1 == no addon selected
  private int currentAddon = -1;

  private PlayerStatus status = new PlayerStatus();
  private PlayerShield shield = new PlayerShield();

  public Player(Vector2D position) {
    this.position = position;
  }

  public int getHealth() {
    return this.health;
  }

  public void display(SpriteBatch batch) {
    if(direction == Direction.LEFT) {
      sprite.flip(true, false);
      sprite.draw(batch);
      sprite.flip(true, false);
    } else {
      sprite.draw(batch);
    }

    if(isUsingAddon(PlayerAddon.ADDON_SHIELD)) {
      shield.display(batch, getCenterX(), getCenterY());
    }
  }

  public void displayOverlay(SpriteBatch batch) {
    status.display(
      batch,
      this.health * 100 / MAX_HEALTH,
      this.score,
      this.battery * 100 / MAX_BATTERY,
      this.addons,
      this.currentAddon
    );
  }

  public void displayInteraction(SpriteBatch batch) {
    if(this.interacting) {
      this.nearestInteractable.displayInteraction(batch);
    }
  }

  /**
   * Move the player back to his initial position.
   */
  public void resetPosition() {
    this.resetPosition = true;
  }

  public void resetInitialPosition(Vector2D initialPosition) {
    this.position = initialPosition;

    this.resetPosition();
  }

  public void update(float deltaTime) {
    if(this.resetPosition) {

      this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
      this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

      this.body.setTransform(
        (this.sprite.getX() + this.sprite.getWidth() / 2f) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
        (this.sprite.getY() + this.sprite.getHeight() / 2f) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
        0
      );

      this.resetPosition = false;
    }

    /*
     * Update the player's coordinates based on its physics body.
     */
    sprite.setPosition(
      (body.getPosition().x * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getWidth()/2,
      (body.getPosition().y * Configuration.Physics.WORLD_UNIT_TO_PIXELS) - sprite.getHeight()/2
    );

    if(isStopped() || !moving) {
      moving = false;
      resetAnimation();
    }

    /*
     * Change the part of the sprite which will be displayed
     * if it's time to move to the next animation frame.
     */
    if(moving) {
      keepMoving();
    }

    if(moving && !isJumping()) {
      regionTime += deltaTime;
      if(regionTime > this.getAnimationStep()) {
        regionIndex++;

        if(regionIndex > LAST_REGION) {
          regionIndex = FIRST_REGION;
        }

        sprite.setRegion(textureAtlas.findRegion(Integer.toString(regionIndex)));

        regionTime = 0f;
      }
    }

    if(isUsingAddon(PlayerAddon.ADDON_SHIELD) || (isUsingAddon(PlayerAddon.ADDON_SPEED) && (moving || isJumping()))) {
      PlayerAddon addon = this.addons.get(currentAddon);

      // if there is no more battery, we drop the addon
      if(!addon.decreaseBattery(1)) {
        this.addons.removeIndex(currentAddon);
        this.currentAddon = -1;
      }
    }

    // the player regains battery power over time
    this.increaseInnerBattery(1);
  }

  public void startMoving(Direction direction) {
    this.moving = true;
    this.direction = direction;

    keepMoving();
  }

  private void keepMoving() {
    if(this.battery < MOVING_POWER_USE/2) {
      return;
    }

    this.decreaseInnerBattery(MOVING_POWER_USE);

    float speed = this.getSpeed();

    this.body.setLinearVelocity(
      direction == Direction.LEFT ? -speed : speed,
      this.body.getLinearVelocity().y
    );

    if(this.pushing) {
      for(LevelBlockMovable movable: this.movables) {
        movable.stick(this);
      }
    }
  }

  public void stopMoving(Direction direction) {
    if(direction == this.direction) {
      this.moving = false;

      this.body.setLinearVelocity(
        0f, this.body.getLinearVelocity().y
      );
    }
  }

  private boolean canJump() {
    return objectsBelowFeet > 0;
  }

  public void jump() {
    if(!canJump()) {
      return;
    }

    // not enough battery
    if(this.battery < JUMPING_POWER_USE/2) {
      return;
    }

    this.body.applyLinearImpulse(
      new Vector2(0, this.getJumpSpeed()),
      body.getWorldCenter(),
      true
    );

    this.decreaseInnerBattery(JUMPING_POWER_USE);

    resetAnimation();
  }

  private void resetAnimation() {
    regionIndex = 1;
    regionTime = 0f;

    sprite.setRegion(textureAtlas.findRegion(Integer.toString(FIRST_REGION)));
  }

  private boolean isStopped() {
    return Math.abs(body.getLinearVelocity().x) < 0.1f;
  }

  private boolean isJumping() {
    /*
     * If the player's vertical speed is 0, he is not jumping.
     * If his player's vertical speed is more than 0 but he is standing on something
     * (e.g. a vertically moving platform), he is not jumping.
     */
    return Math.abs(body.getLinearVelocity().y) > 0.1f && objectsBelowFeet == 0;
  }

  public void increaseHealth(int bonus) {
    this.health = Math.min(this.health + bonus, MAX_HEALTH);
  }

  public void increaseScore(int bonus) {
    this.score += bonus;
  }

  public void increaseInnerBattery(int amount) {
    this.battery = Math.min(this.battery + amount, MAX_BATTERY);
  }

  private void decreaseInnerBattery(int amount) {
    this.battery = Math.max(this.battery - amount, 0);
  }

  /**
   * @return true if the player is alive, false if he is dead after the decrease
   */
  public boolean decreaseHealth(int minus) {
    this.health -= minus;

    if(this.health < 0) {
      GameInstance gameRef = Betaform.getGameRef();
      if(gameRef == null) {
        return false;
      }

      gameRef.leaveGame();
      return false;
    }

    return true;
  }

  /**
   * When the user collects an addon,
   * if he already has an addon of the same type, it is replaced with maximum battery power.
   * Otherwise, we just give him the new addon.
   */
  public void addAddon(PlayerAddon addon) {
    int index = this.addons.indexOf(addon, true);

    if(index == -1) {
      addon.setBatteryToMax();
      this.addons.insert(0, addon);
      currentAddon = 0;
    } else {
      this.addons.get(index).setBatteryToMax();
      this.currentAddon = index;
    }
  }

  /**
   * Select the next addon in the list of the player's addons.
   * Addon number -1 equals to no addon.
   */
  public void changeAddon(boolean backwards) {
    currentAddon += (backwards ? -1 : 1);

    if(currentAddon == -2) {
      currentAddon = addons.size - 1;
    } else if(currentAddon == addons.size) {
      currentAddon = -1;
    }
  }

  public boolean isUsingAddon(PlayerAddon addon) {
    return this.currentAddon != -1 && this.addons.get(currentAddon) == addon;
  }

  private float getSpeed() {
    if(this.isUsingAddon(PlayerAddon.ADDON_SPEED)) {
      return SPEED * 1.3f;
    } else {
      return SPEED;
    }
  }

  private float getJumpSpeed() {
    return this.getSpeed() / 2f;
  }

  private float getAnimationStep() {
    if(this.isUsingAddon(PlayerAddon.ADDON_SPEED)) {
      return ANIMATION_STEP / 2f;
    } else {
      return ANIMATION_STEP;
    }
  }

  public float getCenterX() {
    return this.sprite.getX() + this.sprite.getWidth()/2;
  }

  public float getCenterY() {
    return this.sprite.getY() + this.sprite.getHeight()/2;
  }

  public void addInteractable(LevelInteractable interactable) {
    this.interactables.add(interactable);
    this.nearestInteractable = this.getNearestInteractable();
  }

  public void removeInteractable(LevelInteractable interactable) {
    this.interactables.removeValue(interactable, true);

    // if the player is moving out, stop any interaction
    this.interacting = false;

    this.nearestInteractable = this.getNearestInteractable();
  }

  /**
   * We need this function because the player may be in contact with
   * two or more interactable objects.
   * If he presses the action key, we don't want him to interact with several objects at the same time.
   * This function helps select the nearest object the player is in contact with.
   */
  private LevelInteractable getNearestInteractable() {
    if(this.interactables.size == 0) {
      return null;
    }

    LevelInteractable nearest = this.interactables.get(0);
    float nearestDistance = ((LevelObject)nearest).getDistanceToObject(this);

    for(int index = 1; index < this.interactables.size; index++) {
      LevelInteractable interactable = this.interactables.get(index);

      float newDistance = ((LevelObject)interactable).getDistanceToObject(this);
      if(newDistance < nearestDistance) {
        nearestDistance = newDistance;
        nearest = interactable;
      }
    }

    return nearest;
  }

  public void interact() {
    if(this.isReadyToExit()) {
      GameInstance gameRef = Betaform.getGameRef();
      if(gameRef == null) {
        return;
      }

      gameRef.nextLevel();
      return;
    }

    if(this.nearestInteractable == null) {
      return;
    }

    if(this.nearestInteractable.interact()) {
      this.interacting = false;
    } else {
      this.interacting = true;
    }
  }

  public void startPushing() {
    this.pushing = true;
  }

  public void stopPushing() {
    this.pushing = false;
  }

  public boolean isPushing() {
    return this.pushing;
  }

  public void addMovable(LevelBlockMovable movable) {
    this.movables.add(movable);
  }

  public void removeMovable(LevelBlockMovable movable) {
    this.movables.removeValue(movable, true);
  }

  public Direction getDirection() {
    return this.direction;
  }

  @Override
  public float getWidth() {
    return super.getWidth() - BodyPart.FEET_WIDTH/Configuration.Physics.WORLD_UNIT_TO_PIXELS;
  }

  public float getTrimmedWidth() {
    return super.getWidth() - BodyPart.FEET_WIDTH/Configuration.Physics.WORLD_UNIT_TO_PIXELS;
  }


  public void increaseObjectsBelowFeet() {
    this.objectsBelowFeet++;
  }

  public void decreaseObjectsBelowFeet() {
    this.objectsBelowFeet--;
  }

  /**
   * Instead of using a boolean internally, we use an integer.
   * Since the player has 4 body parts (feet, middle, left, right),
   * we allow him to exit the level only if all his parts are in contact with the exit portal.
   *
   * Otherwise, he may be able to exit it even if only 1 pixel of his rightmost part is in contact with the portal.
   * Hence we increment a counter in order to ch
   *
   */

  public void increasePartsInsideExitPortal() {
    this.partsInsideExitPortal++;
  }

  public void decreasePartsInsideExitPortal() {
    this.partsInsideExitPortal--;
  }

  private boolean isReadyToExit() {
    return this.partsInsideExitPortal == BodyPart.values().length;
  }

  /**
   * If the player shoots a new bullet, where should it appear?
   */
  public Vector2 getNewBulletPosition() {
    float y = body.getPosition().y;
    float x = body.getPosition().x + (direction == Direction.LEFT ? -this.getWidth() : this.getWidth());

    return new Vector2(x, y);
  }

  /**
   * @return true if the player is alive, false otherwise
   */
  private boolean hitBy(int strength, Direction direction) {
    if(this.isUsingAddon(PlayerAddon.ADDON_SHIELD)) {
      return true;
    }

    if(!this.decreaseHealth(strength)) {
      return false;
    }

    float yImpulse = this.getHeight()/2;
    float xImpulse = this.getWidth();

    if(direction == Direction.LEFT) {
      xImpulse = -xImpulse;
    }

    this.body.applyLinearImpulse(
      new Vector2(xImpulse, yImpulse),
      this.body.getWorldCenter(),
      true
    );

    return true;
  }

  public boolean hitBy(LevelEnemy enemy) {
    Direction direction = enemy.getPhysicsPosition().x > body.getPosition().x ? Direction.LEFT : Direction.RIGHT;

    return this.hitBy(enemy.getStrength(), direction);
  }

  @Override
  public boolean hitBy(BulletManager.Bullet bullet) {
    if(!this.hitBy(bullet.strength, bullet.direction)) {
      return false;
    }

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return false;
    }

    gameRef.getBulletManager().destroyBullet(bullet);

    return true;
  }

  private int getStrength() {
    return this.isUsingAddon(PlayerAddon.ADDON_STRENGTH) ? STRENGTH * 2 : STRENGTH;
  }

  public void shoot() {
    BulletManager.BulletType bulletType = BulletManager.BulletType.PLAYER_BULLET;
    if(isUsingAddon(PlayerAddon.ADDON_STRENGTH)) {
      bulletType = BulletManager.BulletType.PLAYER_SUPER_BULLET;

      PlayerAddon addon = this.addons.get(currentAddon);

      // if there is no more battery, we drop the strength addon
      if(!addon.decreaseBattery(30)) {
        this.addons.removeIndex(currentAddon);
        this.currentAddon = -1;
      }
    } else {
      if(this.battery < SHOOTING_POWER_USE/2) {
        return;
      } else {
        this.decreaseInnerBattery(SHOOTING_POWER_USE);
      }
    }

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    gameRef.getBulletManager().newBullet(
      bulletType,
      this.getNewBulletPosition(),
      this.getDirection(),
      this.getStrength()
    );
  }

  /*
   * This function has a side effect:
   * it sets the new player's initial position to be its current position.
   * When the player falls on the ground, he goes back to his initial position.
   * After calling createSave, his initial position is the position of his last save.
   */
  public PlayerSave createSave() {
    Vector2 bodyPosition = this.body.getPosition();

    this.position = new Vector2D(
      (int)(bodyPosition.x * Configuration.Physics.WORLD_UNIT_TO_PIXELS + this.sprite.getWidth()),
      (int)(bodyPosition.y * Configuration.Physics.WORLD_UNIT_TO_PIXELS + this.sprite.getHeight())
    );

    return new PlayerSave(
      new Array<>(this.addons),
      this.currentAddon,
      this.health,
      this.score,
      this.battery,
      new Vector2(this.body.getPosition())
    );
  }

  public void reloadFromSave(PlayerSave save) {
    this.addons = save.getAddons();
    this.currentAddon = save.getCurrentAddon();
    this.health = save.getHealth();
    this.score = save.getScore();
    this.battery = save.getBattery();
    this.body.setTransform(
      save.getPhysicsPosition(),
      0
    );
  }

  /**
   * Shoud be called only once, after or inside {@link #postloadResources()},
   * because creating the physical body requires to know some information
   * about the graphics.
   */
  private void initPhysics() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.fixedRotation = true;

    bodyDef.position.set(
      (this.sprite.getX() + this.sprite.getWidth() / 2f) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      (this.sprite.getY() + this.sprite.getHeight() / 2f) / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    this.body = gameRef.getWorld().createBody(bodyDef);
    this.body.setUserData(this);

    float physicsWidth = this.sprite.getWidth() / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    float physicsWidthTrimmer = physicsWidth - BodyPart.FEET_WIDTH / Configuration.Physics.WORLD_UNIT_TO_PIXELS;

    float physicsHeight = this.sprite.getHeight() / Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    float physicsHeightTrimmer = (this.sprite.getHeight() - 2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS;

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(
      physicsWidthTrimmer / 2,
      physicsHeightTrimmer / 2
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 1f;
    fixtureDef.friction = 0f;
    fixtureDef.restitution = 0f;

    Fixture fixture = body.createFixture(fixtureDef);
    fixture.setUserData(BodyPart.MIDDLE);

    /*
     * Create a foot fixture at the bottom of the player
     * which is half the width of the player.
     */
    shape.setAsBox(
      physicsWidthTrimmer / 2.2f,
      physicsHeight / 10,
      new Vector2(0, - physicsHeight / 2 + physicsHeight/10),
      0
    );

    fixtureDef.friction = 0.2f;

    Fixture feetFixture = this.body.createFixture(fixtureDef);
    feetFixture.setUserData(BodyPart.FEET);

    shape.dispose();

    this.syncGraphicsFromPhysics();
  }

  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
    ResourceLoader loader = ResourceLoader.getInstance();

    if(!loader.isPreloaded(this.getResourceEaterID())) {
      AssetManager manager = ResourceLoader.getInstance().registerForPreloading(this.getResourceEaterID());

      manager.load(this.getPath(), TextureAtlas.class);
    }

    this.status.preloadResources();
    this.shield.preloadResources();
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    AssetManager manager = ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.textureAtlas = manager.get(this.getPath(), TextureAtlas.class);
    this.sprite = new Sprite(this.textureAtlas.findRegion(Integer.toString(FIRST_REGION)));

    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.status.postloadResources();
    this.shield.postloadResources();

    this.initPhysics();
  }

  @Override
  public String getResourceEaterID() { return classID; }

  public boolean isInteracting() {
    return this.interacting;
  }
}

