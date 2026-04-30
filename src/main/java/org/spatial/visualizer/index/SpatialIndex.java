package org.spatial.visualizer.index;

import processing.core.PApplet;
import org.spatial.visualizer.model.SpatialObject;

public interface SpatialIndex<T extends SpatialObject> {
    void insert(T spatialObject);

    void draw(PApplet p);
}
