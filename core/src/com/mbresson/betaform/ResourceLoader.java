package com.mbresson.betaform;

import com.badlogic.gdx.assets.AssetManager;

import com.badlogic.gdx.utils.Array;

/**
 * This class is used to preload classes which need any kind of asset (graphics, audio, fonts, etc.).
 * In order to do so, it makes use of libGDX's AssetManager.
 *
 * To preload itself, a class must first check that it hasn't already preloaded itself.
 * by calling {@link #registerForPreloading(String)}, where String is a unique identifier for this class.
 * Then, if it hasn't preloaded itself, it can get the AssetManager by calling {@link #registerForPreloading(String)}
 * and use it to load assets.
 * In the postloading process, it can get the manager again by calling {@link #registerForPostloading(String)}
 * Typically, the postloading process may do things like fetching the assets loaded with the manager.
 */
public class ResourceLoader {
  
  public class AlreadyPreloadedException extends Exception {
    private static final long serialVersionUID = 4322342342432342423L;

    public AlreadyPreloadedException(String message) {
      super(message);
    }
  }

  public class NotPreloadedYetException extends Exception {
    private static final long serialVersionUID = 4322342342432342424L;

    public NotPreloadedYetException(String message) {
      super(message);
    }
  }

  // this is a singleton
  private ResourceLoader() {
  }

  private static ResourceLoader instance = null;

  public static ResourceLoader getInstance() {
    if(instance != null) {
      return instance;
    } else {
      instance = new ResourceLoader();

      return instance;
    }
  }

  private AssetManager manager = new AssetManager();
  private Array<String> preloadedList = new Array<>(10);

  /**
   * This method must never be called by a class in order to preload itself.
   * Such a class should use {@link #registerForPreloading(String)} instead.
   *
   * This method is only useful for a general game class which needs to get the AssetManager
   * for something else than loading assets (e.g. know the progress of the loading, stop the loading, etc).
   */
  public AssetManager getManager() {
    return this.manager;
  }

  /**
   * @param uniqueID a unique identifier for the class that will preload itself
   *
   * @throws AlreadyPreloadedException if the class has already preloaded itself
   */
  public AssetManager registerForPreloading(String uniqueID) throws AlreadyPreloadedException {
    if(this.isPreloaded(uniqueID)) {
      throw new AlreadyPreloadedException(uniqueID.toString());
    }

    this.preloadedList.add(uniqueID);

    return this.manager;
  }

  /**
   * @param uniqueID the identifier which was given when calling {@link #registerForPreloading(String)}
   *
   * @throws NotPreloadedYetException if the class hasn't preloaded itself yet
   */
  public AssetManager registerForPostloading(String uniqueID) throws NotPreloadedYetException {
    if(!this.isPreloaded(uniqueID)) {
      throw new NotPreloadedYetException(uniqueID.toString());
    }

    return this.manager;
  }

  /**
   * @param uniqueID the identifier which was given when calling {@link #registerForPreloading(String)}
   *
   * @return true if the class has previously called {@link #registerForPreloading(String)}
   */
  public boolean isPreloaded(String uniqueID) {
    return this.preloadedList.contains(uniqueID, false);
  }

  public void dispose() {
    this.manager.dispose();
    this.preloadedList.clear();
  }
}

