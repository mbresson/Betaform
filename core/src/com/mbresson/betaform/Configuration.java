package com.mbresson.betaform;

import com.badlogic.gdx.Input.Keys;

public class Configuration {

  public static class Controls {

    public static final int KEY_MOVE_LEFT = Keys.LEFT;
    public static final int KEY_MOVE_RIGHT = Keys.RIGHT;
    public static final int KEY_JUMP = Keys.SPACE;
    public static final int KEY_LEAVE = Keys.ESCAPE;
    public static final int KEY_ACTION = Keys.CONTROL_LEFT;
    public static final int KEY_SHOOT = Keys.C;
    public static final int KEY_PAUSE = Keys.P;

    // only used in the editor
    public static final int KEY_MOVE_UP = Keys.UP;
    public static final int KEY_MOVE_DOWN = Keys.DOWN;

  }

  public static class Physics {

    public static final float STEPS_PER_SECOND = 45f;
    public static final float WORLD_UNIT_TO_PIXELS = 100;
    public static final float FRAMERATE_STEP = 1.0f / STEPS_PER_SECOND;
    public static final float GRAVITY = -10f;

  }

  public static class Video {

    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;

  }

  public static class Level {

    public static final String FIRST_LEVEL_NAME = "01-tutorial";
    public static final int TILE_WIDTH = 64;
    public static final int TILE_HEIGHT = 64;

  }
}

