package com.mbresson.betaform.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mbresson.betaform.Betaform;
import com.mbresson.betaform.Configuration;

public class DesktopLauncher {
  
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

    config.title = "Betaform";
    config.resizable = false;
    config.foregroundFPS = 30;
    config.height = Configuration.Video.WINDOW_HEIGHT;
    config.width = Configuration.Video.WINDOW_WIDTH;
    config.vSyncEnabled = true;

		new LwjglApplication(new Betaform(), config);
	}
}
