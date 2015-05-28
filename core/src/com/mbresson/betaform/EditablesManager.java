package com.mbresson.betaform;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * This class is responsible for handling editable level items (enemies, artifacts, etc).
 */
public class EditablesManager implements ResourceEater {

  private static final String classID = EditablesManager.class.getName();

  public enum ManagerMode {
    OBJECT_MANAGER,
    TILE_MANAGER
  }

  private ManagerMode mode = ManagerMode.OBJECT_MANAGER;

  private Array<LevelObject> editables = new Array<>(100);
  private Array<LevelTile> tiles = new Array<>(LevelTile.TileType.values().length);
  
  private LevelTileDisplayer tileDisplayer = new LevelTileDisplayer();

  private int currentEditableIndex = 0, currentTileIndex = 0;

  public EditablesManager() {
    this.editables.add(new LevelAddon(LevelAddon.ADDON_SHIELD));
    this.editables.add(new LevelAddon(LevelAddon.ADDON_SPEED));
    this.editables.add(new LevelAddon(LevelAddon.ADDON_STRENGTH));
    this.editables.add(new LevelBonus(LevelBonus.BONUS_BATTERY));
    this.editables.add(new LevelBonus(LevelBonus.BONUS_HEALTH));
    this.editables.add(new LevelBonus(LevelBonus.BONUS_SCORE));
    this.editables.add(new LevelHint());
    this.editables.add(new LevelSave());
    this.editables.add(new LevelDoor());
    this.editables.add(new LevelBlockSwitch());
    this.editables.add(new LevelBlockMoving());
    this.editables.add(new LevelBlockMovable());
    this.editables.add(new LevelEnemyLion());
    this.editables.add(new LevelEnemySheep());
    this.editables.add(new LevelEnemySmartSheep());
    this.editables.add(new LevelEnemyTree());
    this.editables.add(new LevelEnemyTripod());

    for(LevelTile.TileType tileType: LevelTile.TileType.values()) {
      this.tiles.add(new LevelTile(tileType));
    }
  }

  @Override
  public void preloadResources() throws ResourceLoader.AlreadyPreloadedException {
    if(! ResourceLoader.getInstance().isPreloaded(this.getResourceEaterID())) {
      ResourceLoader.getInstance().registerForPreloading(this.getResourceEaterID());
    }

    this.tileDisplayer.preloadResources();

    for(LevelObject editable: this.editables) {
      editable.preloadResources();
    }
  }

  @Override
  public void postloadResources() throws ResourceLoader.NotPreloadedYetException {
    ResourceLoader.getInstance().registerForPostloading(this.getResourceEaterID());

    this.tileDisplayer.postloadResources();

    for(LevelObject editable: this.editables) {
      editable.postloadResources();
    }

    for(LevelTile tile: this.tiles) {
      tile.load(this.tileDisplayer.getSpriteSizeForTile(tile.getType()));
    }
  }

  @Override
  public String getResourceEaterID() { return classID; }

  public void nextEditable() {
    if(this.mode == ManagerMode.OBJECT_MANAGER) {
      this.currentEditableIndex++;
      if(this.currentEditableIndex == this.editables.size) {
        this.currentEditableIndex = 0;
      }
    } else if(this.mode == ManagerMode.TILE_MANAGER) {
      this.currentTileIndex++;
      if(this.currentTileIndex == this.tiles.size) {
        this.currentTileIndex = 0;
      }
    }
  }

  public void previousEditable() {
    if(this.mode == ManagerMode.OBJECT_MANAGER) {
      this.currentEditableIndex--;
      if(this.currentEditableIndex == -1) {
        this.currentEditableIndex = this.editables.size -1;
      }
    } else if(this.mode == ManagerMode.TILE_MANAGER) {
      this.currentTileIndex--;
      if(this.currentTileIndex == -1) {
        this.currentTileIndex = this.tiles.size -1;
      }
    }
  }

  public void displayEditable(SpriteBatch batch, float x, float y) {
    if(this.mode == ManagerMode.OBJECT_MANAGER) {
      LevelObject editable = this.editables.get(this.currentEditableIndex);

      editable.setEditablePosition((int)x, (int)y);
      editable.display(batch);
    } else if(this.mode == ManagerMode.TILE_MANAGER) {
      LevelTile editable = this.tiles.get(this.currentTileIndex);

      editable.setEditablePosition((int)x, (int)y);
      this.tileDisplayer.display(batch, editable);
    }
  }

  public void switchMode() {
    this.mode = this.mode == ManagerMode.OBJECT_MANAGER ? ManagerMode.TILE_MANAGER : ManagerMode.OBJECT_MANAGER;
  }

  public void addEditable(Level level, Vector2 position) {
    position.x = position.x - (position.x % Configuration.Level.TILE_WIDTH);
    position.y = position.y - (position.y % Configuration.Level.TILE_HEIGHT);

    if(this.mode == ManagerMode.OBJECT_MANAGER) {
      LevelObject object = this.editables.get(this.currentEditableIndex);
      object.setEditablePosition((int)position.x, (int)position.y);

      LevelObject newObject = null;

      // TODO clean this huge mess with a method .clone in Editable interface
      if(object instanceof LevelAddon) {
        newObject = new LevelAddon((LevelAddon) object);
      } else if(object instanceof LevelBonus) {
        newObject = new LevelBonus((LevelBonus) object);
      } else if(object instanceof LevelHint) {
        newObject = new LevelHint((LevelHint) object);
      } else if(object instanceof LevelSave) {
        newObject = new LevelSave((LevelSave) object);
      } else if(object instanceof LevelDoor) {
        newObject = new LevelDoor((LevelDoor) object);
      }/* else if(object instanceof LevelBlockMoving) { // FIX
        newObject = new LevelBlockMoving((LevelBlockMoving) object);
      } else if(object instanceof LevelBlockMovable) {
        newObject = new LevelBlockMovable((LevelBlockMovable) object);
      } else if(object instanceof LevelBlockSwitch) {
        newObject = new LevelBlockSwitch((LevelBlockSwitch) object);
      }*/ else {
        return;
      }

      level.addLevelObject(newObject);
    } else if(this.mode == ManagerMode.TILE_MANAGER) {
      LevelTile tile = this.tiles.get(this.currentTileIndex);
      tile.setEditablePosition((int)position.x, (int)position.y);

      LevelTile newTile = new LevelTile(tile);

      level.addLevelTile(newTile);
    }
  }
}

