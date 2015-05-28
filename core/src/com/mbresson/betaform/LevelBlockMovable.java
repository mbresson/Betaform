package com.mbresson.betaform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class LevelBlockMovable extends LevelBlock implements CanHandlePlayerContact {

  public String getType() { return "movable"; } 

  public LevelBlockMovable() {
    super();
    this.type = "movable";
  }

  public LevelBlockMovable(LevelBlockMovable movable) {
    super(movable);
  }

  public void stick(Player player) {
    float playerBottomY = player.body.getPosition().y - player.getHeight()/2;
    float blockBottomY = this.body.getPosition().y - this.getHeight()/2;

    // if the player is standing on the block or just below the block, he cannot push it
    if(!MathUtils.equals(playerBottomY, blockBottomY, 0.1f)) {
      return;
    }

    float playerCenterX = player.body.getPosition().x;
    float playerHalfWidth = player.getWidth() / 2;
    float blockHalfWidth = this.getWidth() / 2;

    if(player.getDirection() == Direction.RIGHT) {
      this.body.setTransform(
        playerCenterX - (playerHalfWidth + blockHalfWidth),
        this.body.getPosition().y,
        this.body.getAngle()
      );
    } else {
      this.body.setTransform(
        playerCenterX + playerHalfWidth + blockHalfWidth,
        this.body.getPosition().y,
        this.body.getAngle()
      );
    }
  }

  @Override
  public void handlePlayerContactBegin(Player player, Contact contact) {
    player.addMovable(this);
  }

  @Override
  public void handlePlayerContactEnd(Player player, Contact contact) {
    player.removeMovable(this);
  }
}

