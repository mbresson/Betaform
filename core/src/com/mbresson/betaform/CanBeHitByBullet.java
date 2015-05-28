package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;

interface CanBeHitByBullet {

  /**
   * @return true if the object is alive, false if it is dead after being hit
   */
  boolean hitBy(BulletManager.Bullet bullet);

}

