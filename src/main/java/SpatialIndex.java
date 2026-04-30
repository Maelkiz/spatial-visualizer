import processing.core.PApplet;

public interface SpatialIndex<T extends SpatialObject> {
    void insert(T spatialObject);

    void draw(PApplet p);
}
