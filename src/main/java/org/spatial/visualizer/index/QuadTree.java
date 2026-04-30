package org.spatial.visualizer.index;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import org.spatial.visualizer.model.SpatialObject;
import org.spatial.visualizer.model.Rectangle;

public class QuadTree<T extends SpatialObject> implements SpatialIndex<T> {
    private final int MAX_POINTS = 4;
    private final float x, y, width, height;
    private final QuadTree<T>[] quadrants;
    private final List<T> objects;

    @SuppressWarnings("unchecked")
    public QuadTree(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.quadrants = (QuadTree<T>[]) new QuadTree[4];
        this.objects = new ArrayList<>();
    }

    public void query(Rectangle range, List<T> found) {
        if (!intersects(range))
            return;

        for (T object : objects) {
            if (range.contains(object.x(), object.y())) {
                found.add(object);
            }
        }

        if (quadrants[0] != null) {
            for (QuadTree<T> child : quadrants) {
                child.query(range, found);
            }
        }
    }

    public void insert(T object) {
        if (!contains(object)) {
            return;
        }

        if (objects.size() < MAX_POINTS) {
            objects.add(object);
            return;
        }

        if (quadrants[0] == null) {
            subdivide();
        }

        for (QuadTree<T> child : quadrants) {
            child.insert(object);
        }
    }

    private void subdivide() {
        float halfWidth = width / 2;
        float halfHeight = height / 2;

        quadrants[0] = new QuadTree<>(x, y, halfWidth, halfHeight); // Top-left
        quadrants[1] = new QuadTree<>(x + halfWidth, y, halfWidth, halfHeight); // Top-right
        quadrants[2] = new QuadTree<>(x, y + halfHeight, halfWidth, halfHeight); // Bottom-left
        quadrants[3] = new QuadTree<>(x + halfWidth, y + halfHeight, halfWidth, halfHeight); // Bottom-right
    }

    public boolean contains(T object) {
        return object.x() >= x
                && object.x() <= x + width
                && object.y() >= y
                && object.y() <= y + height;
    }

    private boolean intersects(Rectangle range) {
        return !(range.x() > x + width ||
                range.x() + range.width() < x ||
                range.y() > y + height ||
                range.y() + range.height() < y);
    }

    public void draw(PApplet p) {
        p.stroke(255, 0, 0);
        p.noFill();
        p.rect(x, y, width, height);
        for (T object : objects) {
            p.fill(255, 0, 0);
            object.draw(p);
        }
        if (quadrants[0] != null) {
            for (QuadTree<T> child : quadrants) {
                child.draw(p);
            }
        }
    }
}
