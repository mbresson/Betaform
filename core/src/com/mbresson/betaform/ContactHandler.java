package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.math.Vector2;

public class ContactHandler implements ContactListener {

  private void beginContactWithFeet(Player player, Object object) {
    // the player can only jump if he is standing on the ground or a block
    if(object instanceof LevelBlock || object instanceof LevelTile) {
      player.increaseObjectsBelowFeet();
    } else if(object instanceof LevelBoundaries.BoundaryType) {
      LevelBoundaries.BoundaryType type = (LevelBoundaries.BoundaryType) object;

      if(object == LevelBoundaries.BoundaryType.GROUND) {
        /*
         * Reset the player's position to the level's entrance spot.
         */
        player.resetPosition();
      }
    }
  }

  @Override
  public void beginContact(Contact contact) {
    GameInstance gameRef = Betaform.getGameRef();

    // if the game is being stopped, return
    if(gameRef == null) {
      return;
    }

    Body bodyA = contact.getFixtureA().getBody();
    Body bodyB = contact.getFixtureB().getBody();

    if(bodyA.getUserData() == null || bodyB.getUserData() == null) {
      return;
    }

    if(bodyA.getUserData() instanceof BulletManager.Bullet) {

      BulletManager.Bullet bullet = (BulletManager.Bullet) bodyA.getUserData();
      Object object = bodyB.getUserData();

      if(object instanceof CanBeHitByBullet) {
        ((CanBeHitByBullet) object).hitBy(bullet);
      } else if(!(object instanceof Traversable)) {
        gameRef.getBulletManager().destroyBullet(bullet);
      }

      return;

    } else if(bodyB.getUserData() instanceof BulletManager.Bullet) {

      BulletManager.Bullet bullet = (BulletManager.Bullet) bodyB.getUserData();
      Object object = bodyA.getUserData();

      if(object instanceof CanBeHitByBullet) {
        ((CanBeHitByBullet) object).hitBy(bullet);
      } else if(!(object instanceof Traversable)) {
        gameRef.getBulletManager().destroyBullet(bullet);
      }

      return;
    }

    if(contact.getFixtureB().getUserData() instanceof Player.BodyPart) {

      Player.BodyPart bodyPart = (Player.BodyPart) contact.getFixtureB().getUserData();
      if(bodyPart == Player.BodyPart.FEET) {
        beginContactWithFeet((Player) bodyB.getUserData(), bodyA.getUserData());
      }

    } else if(contact.getFixtureA().getUserData() instanceof Player.BodyPart) {

      Player.BodyPart bodyPart = (Player.BodyPart) contact.getFixtureA().getUserData();
      if(bodyPart == Player.BodyPart.FEET) {
        beginContactWithFeet((Player) bodyA.getUserData(), bodyB.getUserData());
      }
    }


    if(bodyA.getUserData() instanceof Player) {

      Player player = (Player) bodyA.getUserData();
      Object object = bodyB.getUserData();

      if(object instanceof CanHandlePlayerContact) {
        ((CanHandlePlayerContact) object).handlePlayerContactBegin(player, contact);
      }

    } else if(bodyB.getUserData() instanceof Player) {

      Player player = (Player) bodyB.getUserData();
      Object object = bodyA.getUserData();

      if(object instanceof CanHandlePlayerContact) {
        ((CanHandlePlayerContact) object).handlePlayerContactBegin(player, contact);
      }
    }
  }

  private void endContactWithFeet(Player player, Object object) {
    // the player can only jump if he is standing on the ground or a block
    if(object instanceof LevelBlock || object instanceof LevelTile) {
      player.decreaseObjectsBelowFeet();
    }
  }

  @Override
  public void endContact(Contact contact) {
    Body bodyA = contact.getFixtureA().getBody();
    Body bodyB = contact.getFixtureB().getBody();

    if(bodyA.getUserData() == null || bodyB.getUserData() == null) {
      return;
    }

    if(contact.getFixtureB().getUserData() instanceof Player.BodyPart) {

      Player.BodyPart bodyPart = (Player.BodyPart) contact.getFixtureB().getUserData();
      if(bodyPart == Player.BodyPart.FEET) {
        endContactWithFeet((Player) bodyB.getUserData(), bodyA.getUserData());
      }

    } else if(contact.getFixtureA().getUserData() instanceof Player.BodyPart) {

      Player.BodyPart bodyPart = (Player.BodyPart) contact.getFixtureA().getUserData();
      if(bodyPart == Player.BodyPart.FEET) {
        endContactWithFeet((Player) bodyA.getUserData(), bodyB.getUserData());
      }
    }

    if(bodyA.getUserData() instanceof Player) {

      Player player = (Player) bodyA.getUserData();
      Object object = bodyB.getUserData();

      if(object instanceof CanHandlePlayerContact) {
        ((CanHandlePlayerContact) object).handlePlayerContactEnd(player, contact);
      }
    } else if(bodyB.getUserData() instanceof Player) {

      Player player = (Player) bodyB.getUserData();
      Object object = bodyA.getUserData();

      if(object instanceof CanHandlePlayerContact) {
        ((CanHandlePlayerContact) object).handlePlayerContactEnd(player, contact);
      }
    }
  }

  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {
  }

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {
  }

}

