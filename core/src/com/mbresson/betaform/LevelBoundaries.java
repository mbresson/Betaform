package com.mbresson.betaform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.EnumMap;

/**
 * This class is used to create an invisible ground, ceiling and side limits
 * preventing the player from walking out of the level.
 */
public class LevelBoundaries implements ResourceEater {

  private static final String classID = LevelBoundaries.class.getName();

  private static final int THICKNESS = 1;

  public static enum BoundaryType {
    GROUND,
    CEILING,
    LEFT_WALL,
    RIGHT_WALL
  }

  private EnumMap<BoundaryType, Body> bodies;
  private int levelWidth, levelHeight;

  public LevelBoundaries(int levelWidth, int levelHeight) {
    this.bodies = new EnumMap<>(BoundaryType.class);

    this.levelWidth = levelWidth;
    this.levelHeight = levelHeight;
  }

  private void initPhysics() {
    for(BoundaryType type: BoundaryType.values()) {
      this.bodies.put(type, LevelBoundaries.createBody(type, this.levelWidth, this.levelHeight));
    }
  }

  private static Body createBody(BoundaryType type, int levelWidth, int levelHeight) {
    BodyDef bodyDef = new BodyDef();

    bodyDef.type = BodyDef.BodyType.StaticBody;

    switch(type) {
      case GROUND:
        bodyDef.position.set(0, -THICKNESS); // the ground is just below the level
        break;

      case CEILING:
        bodyDef.position.set(0, levelHeight / Configuration.Physics.WORLD_UNIT_TO_PIXELS + THICKNESS); // the ceiling is just above the level
        break;

      case LEFT_WALL:
        bodyDef.position.set(-THICKNESS, 0); // the left wall is just left of the level
        break;

      case RIGHT_WALL:
        bodyDef.position.set(levelWidth / Configuration.Physics.WORLD_UNIT_TO_PIXELS + THICKNESS, 0); // the right wall is just right of the level
        break;
    }

    GameInstance gameRef = Betaform.getGameRef();
    if(gameRef == null) {
      System.out.println("Nothing");
      return null;
    }

    Body body = gameRef.getWorld().createBody(bodyDef);
    body.setUserData(type);

    PolygonShape shape = new PolygonShape();

    switch(type) {
      case GROUND:
      case CEILING:
        // the ground and the ceiling are as wide as the level and 1 meter high
        shape.setAsBox(levelWidth / Configuration.Physics.WORLD_UNIT_TO_PIXELS, THICKNESS);
        break;

      case LEFT_WALL:
      case RIGHT_WALL:
        // the walls are as high as the level and 1 meter thick
        shape.setAsBox(THICKNESS, levelHeight / Configuration.Physics.WORLD_UNIT_TO_PIXELS);
        break;
    }

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 0f;
    fixtureDef.restitution = 0f;

    switch(type) {
      case GROUND:
        fixtureDef.friction = 0.2f;
        break;

      default:
        fixtureDef.friction = 0f;
        break;
    }

    Fixture fixture = body.createFixture(fixtureDef);

    shape.dispose();

    return body;
  }

  /**
   * If the camera shows some bits outside of the level,
   * move it to stay inside its boundaries.
   */
  public void limitCamera(OrthographicCamera camera) {
    Vector3 cameraPos = camera.position;

    float windowHalfWidth = Gdx.graphics.getWidth()/2;
    float windowHalfHeight = Gdx.graphics.getHeight()/2;

    float rightmostX = bodies.get(BoundaryType.RIGHT_WALL).getPosition().x * Configuration.Physics.WORLD_UNIT_TO_PIXELS;
    float topmostY = bodies.get(BoundaryType.CEILING).getPosition().y * Configuration.Physics.WORLD_UNIT_TO_PIXELS;

    if(cameraPos.x < windowHalfWidth) {
      cameraPos.x = windowHalfWidth;
    } else if(cameraPos.x + windowHalfWidth > levelWidth) {
      cameraPos.x = levelWidth - windowHalfWidth;
    }

    if(cameraPos.y < windowHalfHeight) {
      cameraPos.y = windowHalfHeight;
    } else if(cameraPos.y + windowHalfHeight > levelHeight) {
      cameraPos.y = levelHeight - windowHalfHeight;
    }
  }

  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
    if(! ResourceLoader.getInstance().isPreloaded(this.getResourceEaterID())) {
      ResourceLoader.getInstance().registerForPreloading(this.getResourceEaterID());
    }
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.initPhysics();
  }

  @Override
  public String getResourceEaterID() { return classID; }
}


