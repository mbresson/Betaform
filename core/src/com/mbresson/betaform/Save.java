package com.mbresson.betaform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class Save {

  private static final String SAVE_PATH = "data/save/save.json";

  private String level;
  private PlayerSave playerSave;

  public Save() {
    this.playerSave = new PlayerSave();
    this.level = "";
  }

  public Save(PlayerSave playerSave, String level) {
    this.playerSave = playerSave;
    this.level = level;
  }

  public static boolean saveExists() {
    return Gdx.files.local(SAVE_PATH).exists();
  }

  public static Save readFromFile() {
    Json json = new Json();
    Save save = json.fromJson(Save.class, Gdx.files.local(SAVE_PATH));

    return save;
  }

  public void persist() {
    Json json = new Json();
    json.setOutputType(JsonWriter.OutputType.json);

    FileHandle file = Gdx.files.local(SAVE_PATH);
    file.writeString(json.prettyPrint(this), false);

    System.out.println("@Save written at " + SAVE_PATH);
  }

  public PlayerSave getPlayerSave() {
    return this.playerSave;
  }

  public String getLevel() {
    return this.level;
  }
}

