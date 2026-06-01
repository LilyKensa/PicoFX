package dev.huey.picofx.api.bases;

import java.util.Objects;

public class Vec {
  
  static public class Int {
    
    static public Int zero() {
      return new Int(0, 0);
    }
    
    static public Int of(double x, double y) {
      return new Int(x, y);
    }
    
    public int x, y;
    
    public Int(int x, int y) {
      this.x = x;
      this.y = y;
    }
    
    public Int(double x, double y) {
      this((int) x, (int) y);
    }
    
    @Override
    public Int clone() {
      return new Int(x, y);
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj.getClass() == getClass()) {
        Int that = (Int) obj;
        return this.x == that.x && this.y == that.y;
      }
      return false;
    }
    
    public Vec toDouble() {
      return new Vec(x, y);
    }
  }
  
  static public Vec zero() {
    return new Vec(0, 0);
  }
  
  static public Vec of(double x, double y) {
    return new Vec(x, y);
  }
  
  static public Vec ofPolar(double radians, double radius) {
    return new Vec(
      radius * Math.cos(radians),
      radius * Math.sin(radians)
    );
  }
  
  static public Vec ofPolar(double radians) {
    return ofPolar(radians, 1);
  }
  
  static public Int ofInt(double x, double y) {
    return Int.of(x, y);
  }
  
  public double x, y;
  
  public Vec(double x, double y) {
    assign(x, y);
  }
  
  public void assign(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  @Override
  public Vec clone() {
    return new Vec(x, y);
  }
  
  public Int toInt() {
    return new Int(x, y);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) return true;
    if (that == null || getClass() != that.getClass()) return false;

    Vec vec = (Vec) that;
    return Double.compare(vec.x, x) == 0 && Double.compare(vec.y, y) == 0;
  }
  
  public Vec add(Vec that) {
    return new Vec(this.x + that.x, this.y + that.y);
  }
  
  public Vec add(double x, double y) {
    return add(new Vec(x, y));
  }
  
  public Vec multiply(double ratio) {
    return new Vec(this.x * ratio, this.y * ratio);
  }
  
  public Vec minus(Vec that) {
    return add(that.multiply(-1));
  }
  
  public Vec minus(double x, double y) {
    return minus(new Vec(x, y));
  }
  
  public Vec divide(double ratio) {
    return multiply(1 / ratio);
  }

  public Vec mod(Vec grid) {
    if (grid.x == 0 && grid.y == 0) {
      return clone();
    }
    else if (grid.x == 0) {
      return new Vec(x, y % grid.y);
    }
    else if (grid.y == 0) {
      return new Vec(x % grid.x, y);
    }

    return new Vec(x % grid.x, y % grid.y);
  }
  
  public double dot(Vec that) {
    return this.x * that.x + this.y * that.y;
  }
  
  public double lenSq() {
    return dot(this);
  }
  
  public double len() {
    return Math.sqrt(lenSq());
  }
  
  public double angle(Vec origin) {
    return Math.atan2(this.y - origin.y, this.x - origin.x);
  }
  
  public double angle() {
    return angle(zero());
  }

  public boolean isZero() {
    return lenSq() < 1e-9;
  }
  
  public boolean isUnit() {
    return Math.abs(lenSq() - 1) < 1e-9;
  }
  
  public Vec normalize() {
    if (isUnit()) return clone();
    return divide(len());
  }
  
  public Vec resize(double length) {
    return normalize().multiply(length);
  }
  
  public Vec mirror(double radians) {
    double tau = 2 * radians;
    return new Vec(
      x * Math.cos(tau) + y * Math.sin(tau),
      x * Math.sin(tau) - y * Math.cos(tau)
    );
  }
  
  public Vec mirror() {
    return mirror(Math.PI / 2);
  }
  
  public Vec reflect(Vec normal) {
    Vec n = normal.normalize();
    return minus(n.multiply(2 * dot(n)));
  };
  
  public double distSq(Vec that) {
    return minus(that).lenSq();
  }

  public double distSq(double x, double y) {
    return distSq(new Vec(x, y));
  }
  
  public double dist(Vec that) {
    return Math.sqrt(distSq(that));
  }

  public double dist(double x, double y) {
    return dist(new Vec(x, y));
  }
  
  public double approxDist(Vec that) {
    double dx = Math.abs(this.x - that.x), dy = Math.abs(this.y - that.y);
    return 0.9604 * Math.max(dx, dy) + 0.3978 * Math.min(dx, dy);
  }
  
  public boolean inRange(Vec that, double radius) {
    return distSq(that) <= radius * radius;
  }
  
  public boolean inRange(double radius) {
    return inRange(zero(), radius);
  }
  
  public boolean inDonut(Vec that, double inner, double outer) {
    return inRange(that, outer) && !inRange(that, inner);
  }
  
  public boolean inDonut(double inner, double outer) {
    return inDonut(zero(), inner, outer);
  }
  
  public Vec middle(Vec that) {
    return add(that).divide(2);
  }
}
