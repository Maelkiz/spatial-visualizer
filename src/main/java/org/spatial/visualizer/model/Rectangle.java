package org.spatial.visualizer.model;

import java.util.function.Supplier;
import processing.core.PApplet;

public record Rectangle(float x, float y, float width, float height) implements SpatialObject {

    public boolean contains(Point point) {
        return point.x() >= x && point.x() < x + width &&
                point.y() >= y && point.y() < y + height;
    }

    public boolean contains(float x, float y) {
        return x>= this.x && x < this.x + width &&
                y >= this.y && y < this.y + height;
    }

    public boolean intersects(Rectangle other) {
        return !(other.x > x + width ||
                other.x + other.width < x ||
                other.y > y + height ||
                other.y + other.height < y);
    }

    @Override 
    public void draw(PApplet p) {
        p.push();
        p.stroke(255);
        p.noFill();
        p.rect(x, y, width, height);
        p.pop();
    }

    @Override 
    public Rectangle getAABB() {
        return this;
    }
}
