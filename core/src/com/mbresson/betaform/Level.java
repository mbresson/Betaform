package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;

public class Level implements ResourceEater, Json.Serializable {

  private static final String classID = Level.class.getName();

  private int width, height;

  private String levelName;
  private String nextlevel;

  private LevelPortal entrance = new LevelPortal(), exit = new LevelPortal();

  private Array<LevelTile> tiles = new Array<>(200);
  private LevelTileDisplayer tileDisplayer;

  private Array<LevelObject> objects = new Array<>(200);

  private Array<LevelObject> objectsToBeDestroyed = new Array<>(10);

  private LevelBoundaries boundaries;

  public LevelPortal getEntrance() {
    return this.entrance;
  }

  public LevelPortal getExit() {
    return this.exit;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public LevelBoundaries getBoundaries() {
    return this.boundaries;
  }

  public String getLevelName() {
    return this.levelName;
  }

  public void setLevelName(String levelName) {
    this.levelName = levelName;
  }

  public String getNextlevel() {
    return this.nextlevel;
  }

  public static boolean levelExists(String levelName) {
    return Gdx.files.internal("data/levels/" + levelName + ".json").exists();
  }

  /**
   * Create a basic level.
   *
   * @param width in number of horizontal tiles
   * @param height in number of vertical tiles
   */
  public static Level createBaseLevel(int width, int height, String levelName) {
    Level level = new Level();

    level.setWidth(width * Configuration.Level.TILE_WIDTH);
    level.setHeight(height * Configuration.Level.TILE_HEIGHT);
    level.setLevelName(levelName);

    level.boundaries = new LevelBoundaries(level.width, level.height);
    level.tileDisplayer = new LevelTileDisplayer();

    level.entrance = new LevelPortal();
    level.entrance.setPosition(new Vector2D(0, Configuration.Level.TILE_HEIGHT));

    level.exit = new LevelPortal();
    level.exit.setPosition(new Vector2D(200, Configuration.Level.TILE_HEIGHT));

    level.tiles.add(new LevelTile(0, 0, width, 1));

    return level;
  }

  /**
   * Read a level from a json file.
   *
   * @param levelName The name of the level, without its extension (e.g. '01-tutorial')
   */
  public static Level read(String levelName) {
    Json json = new Json();

    /*
     * In json level files, 'class' fields help select
     * the right class to instantiate for the object.
     * (e.g. in a level file, class: "addon" will map to LevelAddon)
     */
    for(LevelObjectType type: LevelObjectType.values()) {
      json.addClassTag(
        type.getLabel(),
        type.getLevelObjectClass()
      );
    }

    for(BehaviorType behaviorType: BehaviorType.values()) {
      json.addClassTag(
        behaviorType.getLabel(),
        behaviorType.getBehaviorClass()
      );
    }

    Level level = json.fromJson(
      Level.class,
      Gdx.files.internal("data/levels/" + levelName + ".json")
    );

    level.boundaries = new LevelBoundaries(level.width, level.height);
    level.tileDisplayer = new LevelTileDisplayer();

    level.levelName = levelName;

    return level;
  }

  /**
   * Writes the level to a level-name.json file.
   */
  public void writeToFile() {
    Json json = new Json();
    json.setOutputType(JsonWriter.OutputType.json);

    String path = "data/levels/" + this.levelName + ".json";

    FileHandle file = Gdx.files.local(path);
    file.writeString(json.prettyPrint(this), false);

    System.out.println("@Level written at " + path);
  }

  public void display(SpriteBatch batch) {
    for(LevelTile tile: tiles) {
      tileDisplayer.display(batch, tile);
    }

    entrance.display(batch);
    exit.display(batch);

    for(LevelObject object: objects) {
      object.display(batch);
    }
  }

  public void update(float deltaTime) {
    for(LevelObject object: objectsToBeDestroyed) {
      this.objects.removeValue(object, true);
      object.dispose();
    }
    objectsToBeDestroyed.clear();

    for(LevelObject object: objects) {
      object.update(deltaTime);
    }
  }

  @Override
  public String toString() {
    Json json = new Json();
    return json.prettyPrint(this);
  }

  public void removeObject(LevelObject object) {
    /*
     * We queue the objects to be destroyed:
     * we can't destroy them while the physics engine is in the middle of its computations.
     */

    /*
     * Also, make sure an object is not set to be destroyed twice:
     * it can happen if several fixtures of the player are in contact with the object at the same time
     * and call the removeObject function at the same time.
     */
    if(this.objectsToBeDestroyed.contains(object, true)) {
      return;
    }

    objectsToBeDestroyed.add(object);
  }

  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
    if(! ResourceLoader.getInstance().isPreloaded(this.getResourceEaterID())) {
      ResourceLoader.getInstance().registerForPreloading(this.getResourceEaterID());
    }

    this.boundaries.preloadResources();
    this.entrance.preloadResources();
    this.exit.preloadResources();
    this.tileDisplayer.preloadResources();

    for(LevelObject object: objects) {
      object.preloadResources();
    }

    this.entrance.setType(LevelPortal.PortalType.ENTRANCE);
    this.exit.setType(LevelPortal.PortalType.EXIT);
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.boundaries.postloadResources();
    this.entrance.postloadResources();
    this.exit.postloadResources();
    this.tileDisplayer.postloadResources();

    Array<LevelDoor> foundDoors = new Array<>(10);
    for(LevelObject object: objects) {
      if(object instanceof LevelDoor) {
        foundDoors.add((LevelDoor) object);
      }
    }

    for(LevelObject object: objects) {
      object.postloadResources();

      /*
       * For each switch in the level, connect it to its door by
       * changing the door's information on the number of switches
       * it requires to be opened.
       */
      if(object instanceof LevelBlockSwitch) {
        LevelBlockSwitch blockSwitch = (LevelBlockSwitch) object;

        int doorNumber = blockSwitch.getDoor();
        for(LevelDoor door: foundDoors) {
          if(door.getNumber() == doorNumber) {
            door.increaseMaxOpeners();

            /*
             * Also, keep a reference to the door in the switch object
             * so that when the switch is switched on, it can notify the door.
             */
            blockSwitch.setDoorRef(door);
            break;
          }
        }
      }
    }

    for(LevelTile tile: this.tiles) {
      tile.load(this.tileDisplayer.getSpriteSizeForTile(tile.getType()));
    }
  }

  @Override
  public String getResourceEaterID() { return classID; }

  @Override
  public void write(Json json) {
    /*
     * TODO: group all tiles in horizontal groups to make collision detection easier
     * and avoid bugs.
     */

    /*
     * In json level files, 'class' fields help select
     * the right class to instantiate for the object.
     * (e.g. in a level file, class: "addon" will map to LevelAddon)
     */
    for(LevelObjectType type: LevelObjectType.values()) {
      json.addClassTag(
        type.getLabel(),
        type.getLevelObjectClass()
      );
    }

    for(BehaviorType behaviorType: BehaviorType.values()) {
      json.addClassTag(
        behaviorType.getLabel(),
        behaviorType.getBehaviorClass()
      );
    }

    json.writeValue("width", this.width);
    json.writeValue("height", this.height);
    json.writeValue("nextlevel", this.nextlevel);
    json.writeValue("entrance", this.entrance);
    json.writeValue("exit", this.exit);
    json.writeValue("objects", this.objects);
    json.writeValue("tiles", this.tiles);
  }

  @Override
  public void read(Json json, JsonValue jsonMap) {
    json.readFields(this, jsonMap);
  }

  public void addLevelObject(LevelObject object) {
    this.objects.add(object);
  }

  public void addLevelTile(LevelTile tile) {
    this.tiles.add(tile);
  }

}

