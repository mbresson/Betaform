package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.*;

/**
 * This class is used to represent an abstract kind of object
 * which can be collected by the player (e.g. a health bonus).
 */
public class LevelPortal extends LevelObject implements ResourceEater, Traversable, CanHandlePlayerContact {

  private static final String classID = LevelPortal.class.getName();

  public enum PortalType {
    ENTRANCE("entrance"),
    EXIT("exit");

    private String type;

    private PortalType(String type) {
      this.type = type;
    }

    public String getLabel() {
      return this.type;
    }
  }

  private PortalType type = PortalType.ENTRANCE;

  protected String getPath() { return "img/sprites/portals.atlas"; }

  private TextureAtlas textureAtlas;

  public void setType(PortalType type) {
    this.type = type;
  }

  public PortalType getType() {
    return this.type;
  }

  public void display(SpriteBatch batch) {
    sprite.draw(batch);
  }

  @Override
  public void handlePlayerContactBegin(Player player, Contact contact) {
    if(this.getType() == LevelPortal.PortalType.EXIT) {
      player.increasePartsInsideExitPortal();
    }
  }

  @Override
  public void handlePlayerContactEnd(Player player, Contact contact) {
    if(this.getType() == LevelPortal.PortalType.EXIT) {
      player.decreasePartsInsideExitPortal();
    }
  }

  /**
   * Shoud be called only once, after or inside {@link #postloadResources()},
   * because creating the physical body requires to know some information
   * about the graphics.
   */
  private void initPhysics() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;

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
    shape.setAsBox(
      (sprite.getWidth()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS,
      (sprite.getHeight()/2) / Configuration.Physics.WORLD_UNIT_TO_PIXELS
    );

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 1f;
    fixtureDef.isSensor = true; // traversable

    Fixture fixture = body.createFixture(fixtureDef);

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
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    AssetManager manager = ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.textureAtlas = manager.get(this.getPath(), TextureAtlas.class);
    this.sprite = new Sprite(this.textureAtlas.findRegion(this.type.getLabel()));
    this.sprite.setX(this.position.getX() + this.sprite.getWidth()/2);
    this.sprite.setY(this.position.getY() + this.sprite.getHeight()/2);

    this.initPhysics();
  }

  @Override
  public String getResourceEaterID() { return classID; }
}


