package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.math.Vector2;

public class LevelEnemyTree extends LevelEnemy {

  private static final String classID = LevelEnemyTree.class.getName();

  @Override
  protected String getPath() { return "img/sprites/robot-tree.atlas"; }

  @Override
  protected int getMaxHealth() { return 60; }

  private float shootingDelay = 1.5f, timeSincePreviousShoot = 0f;

  @Override
  public void changeDirection(Direction direction) {
    this.direction = direction;
  }

  @Override
  protected BodyDef.BodyType getBodyType() { return BodyDef.BodyType.StaticBody; }

  @Override
  public void update(float deltaTime) {
    super.update(deltaTime);

    this.timeSincePreviousShoot += deltaTime;

    float radiusY = this.sprite.getHeight() / Configuration.Physics.WORLD_UNIT_TO_PIXELS * 2;

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      return;
    }

    Player player = gameRef.getPlayer();

    float playerY = player.getPhysicsPosition().y;
    float enemyY = this.body.getPosition().y;

    // the tree must face the player
    Direction direction = this.getDirectionToPlayer(player);
    Vector2 position = new Vector2(
      this.body.getPosition().x + (direction == Direction.LEFT ? -this.getWidth() : this.getWidth()),
      enemyY
    );

    // if the player is not too far from the tree (vertically), shoot bullets towards him
    if(Math.abs(enemyY - playerY) < radiusY) {
      if(this.timeSincePreviousShoot > this.shootingDelay) {
        gameRef.getBulletManager().newBullet(
          BulletManager.BulletType.ENEMY_BULLET,
          position,
          direction,
          20
        );
        this.timeSincePreviousShoot = 0f;
      }
    }
  }

  @Override
  public String getResourceEaterID() { return classID; }
}


