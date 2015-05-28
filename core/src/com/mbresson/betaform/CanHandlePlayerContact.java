package com.mbresson.betaform;

import com.badlogic.gdx.physics.box2d.*;

interface CanHandlePlayerContact {

  // called when the player starts being in contact with the object
  void handlePlayerContactBegin(Player player, Contact contact);

  // called at the end of the contact (e.g. the player is moving away)
  void handlePlayerContactEnd(Player player, Contact contact);

}

