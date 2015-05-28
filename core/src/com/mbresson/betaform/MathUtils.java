package com.mbresson.betaform;

public class MathUtils {

  public static final float EPSILON = 0.001f;

  public static boolean equals(float a, float b, float epsilon) {
    return Math.abs(a - b) < epsilon;
  }

  public static boolean equals(float a, float b) {
    return equals(a, b, EPSILON);
  }

}

