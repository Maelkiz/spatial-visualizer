import processing.core.PApplet;

public class KDTree<T extends SpatialObject> implements SpatialIndex<T> {
    private Node root;

    public KDTree() {
        this.root = null;
    }

    public void insert(T object) {
        root = insert(root, object, 0);
    }

    private Node insert(Node node, T object, int depth) {
        if (node == null) {
            return new Node(object);
        }

        if (node.object.equals(object)) {
            return node; // Avoid duplicates
        }

        int axis = depth % 2;
        if ((axis == 0 && object.x() < node.object.x()) || (axis == 1 && object.y() < node.object.y())) {
            node.left = insert(node.left, object, depth + 1);
        } else {
            node.right = insert(node.right, object, depth + 1);
        }
        return node;
    }

    public boolean contains(T object) {
        return contains(root, object, 0);
    }

    private boolean contains(Node node, T object, int depth) {
        if (node == null)
            return false;
        if (node.object.equals(object))
            return true;

        int axis = depth % 2;
        if ((axis == 0 && object.x() < node.object.x()) || (axis == 1 && object.y() < node.object.y())) {
            return contains(node.left, object, depth + 1);
        } else {
            return contains(node.right, object, depth + 1);
        }
    }

    public T nearest(T target) {
        return nearest(root, target, 0, null, Double.POSITIVE_INFINITY);
    }

    private T nearest(Node node, T target, int depth, T best, double bestDist) {
        if (node == null)
            return best;

        double dist = distanceSquared(node.object, target);
        if (dist < bestDist) {
            bestDist = dist;
            best = node.object;
        }

        int axis = depth % 2;
        Node nearChild = (axis == 0 && target.x() < node.object.x()) || (axis == 1 && target.y() < node.object.y())
                ? node.left
                : node.right;
        Node farChild = nearChild == node.left ? node.right : node.left;

        best = nearest(nearChild, target, depth + 1, best, bestDist);
        bestDist = distanceSquared(best, target);

        double axisDist = axis == 0 ? Math.pow(target.x() - node.object.x(), 2)
                : Math.pow(target.y() - node.object.y(), 2);
        if (axisDist < bestDist) {
            best = nearest(farChild, target, depth + 1, best, bestDist);
        }

        return best;
    }

    private double distanceSquared(T a, T b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return dx * dx + dy * dy;
    }

    private class Node {
        T object;
        Node left, right;

        Node(T object) {
            this.object = object;
        }
    }

    public void draw(PApplet p) {
        drawNode(p, root, 0, 0, p.width, 0, p.height);
    }

    private void drawNode(PApplet p, Node node, int depth, float minX, float maxX, float minY, float maxY) {
        if (node == null)
            return;

        int axis = depth % 2;
        float x = node.object.x();
        float y = node.object.y();

        node.object.draw(p); // Draw the object

        // Draw the splitting line
        if (axis == 0) {
            // Vertical line (splitting on x)
            p.stroke(255, 0, 0); // red
            p.line(x, minY, x, maxY);
            drawNode(p, node.left, depth + 1, minX, x, minY, maxY);
            drawNode(p, node.right, depth + 1, x, maxX, minY, maxY);
        } else {
            // Horizontal line (splitting on y)
            p.stroke(255, 0, 0); // red
            p.line(minX, y, maxX, y);
            drawNode(p, node.left, depth + 1, minX, maxX, minY, y);
            drawNode(p, node.right, depth + 1, minX, maxX, y, maxY);
        }
    }
}
