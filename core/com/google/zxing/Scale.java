package com.google.zxing;

public class Scale {
  private final float x;
  private final float y;
  
  public Scale(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getScaleX() {
    return x;
  }

  public float getScaleY() {
    return y;
  }
  
  @Override 
  public boolean equals(Object other) {
    if (other instanceof Scale) {
      Scale s = (Scale) other;
      return x == s.x && y == s.y;
    }
    return false;
  }

  @Override 
  public int hashCode() {
      return Float.floatToIntBits(x) * 32713 + Float.floatToIntBits(y);
  }

  @Override
  public String toString() {
    return x + "x / " + y + "x";
  }

}
