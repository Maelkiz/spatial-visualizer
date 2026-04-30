package org.spatial.visualizer.model;

import processing.core.PApplet;

public interface SpatialObject {
    Rectangle getAABB();

    void draw(PApplet p);

    float x();
    
    float y();
}
