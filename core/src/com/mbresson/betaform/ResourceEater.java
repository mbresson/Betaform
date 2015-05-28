package com.mbresson.betaform;

interface ResourceEater {

  /**
   * This method must be called after the object has been constructed.
   */
  void preloadResources() throws ResourceLoader.AlreadyPreloadedException;

  /**
   * This method must be called after the object has been created.
   */
  void postloadResources() throws ResourceLoader.NotPreloadedYetException;

  /**
   * @return a unique class identifier
   */
  String getResourceEaterID();

}


