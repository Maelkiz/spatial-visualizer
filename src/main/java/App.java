import processing.core.PApplet;

public class App extends PApplet {
    private SpatialIndex<SpatialObject> spatialIndex;
    private int numObjects = 2500;

    public static void main(String[] args) {
        PApplet.main("App");
    }

    @Override
    public void settings() {
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        spatialIndex = buildSpatialIndex(new QuadTree<>(0, 0, width, height), numObjects);
        keyRepeatEnabled = true;
    }

    @Override
    public void draw() {
        background(0);
        spatialIndex.draw(this);
    }

    @Override
    public void keyPressed() {
        if (key == 's') {
            Util.saveFrame(this, spatialIndex.getClass().getSimpleName());
        }

        if (key == '1') {
            spatialIndex = buildSpatialIndex(new QuadTree<>(0, 0, width, height), numObjects);
        }
        if (key == '2') {
            spatialIndex = buildSpatialIndex(new KDTree<>(), numObjects);
        }
        if (key == '3') {
            spatialIndex = buildSpatialIndex(new RTree<>(), numObjects);
        }
    }

    private SpatialIndex<SpatialObject> buildSpatialIndex(SpatialIndex<SpatialObject> spatialIndex, int numObjects) {
        long seed = System.currentTimeMillis();
        noiseSeed(seed);

        float scale = 0.005f; // Lower = smoother gradients

        for (int i = 0; i < numObjects * 2; i++) {
            float seedX = random(width);
            float seedY = random(height);
            float noiseValue = noise(seedX * scale, seedY * scale);

            if (noiseValue > 0.5f) {
                SpatialObject object = new Point(seedX, seedY);
                //SpatialObject object = new Rectangle(seedX, seedY, random(10, 50), random(10, 50));
                spatialIndex.insert(object);
            }
        }

        return spatialIndex;
    }

}
