import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class RTree<T extends SpatialObject> implements SpatialIndex<T> {
    private static final int MAX_ENTRIES = 4;
    private static final int MIN_ENTRIES = MAX_ENTRIES / 2;

    private Node root;

    public RTree() {
        this.root = new LeafNode();
    }

    public void insert(T object) {
        Entry<T> entry = new Entry<>(object);
        Split split = root.insert(entry);
        if (split != null) {
            // Create new root
            InternalNode newRoot = new InternalNode();
            newRoot.children.add(split.left);
            newRoot.children.add(split.right);
            newRoot.recalculateMBR();
            root = newRoot;
        }
    }

    public List<T> search(Rectangle range) {
        List<T> results = new ArrayList<>();
        root.search(range, results);
        return results;
    }

    private abstract class Node {
        Rectangle mbr;

        abstract Split insert(Entry<T> entry);

        abstract void search(Rectangle range, List<T> results);

        abstract void recalculateMBR();

        abstract List<Entry<T>> getEntries();
    }

    private class LeafNode extends Node {
        List<Entry<T>> entries = new ArrayList<>();

        @Override
        Split insert(Entry<T> entry) {
            entries.add(entry);
            if (mbr == null) {
                mbr = entry.mbr;
            } else {
                mbr = combine(mbr, entry.mbr);
            }

            if (entries.size() > MAX_ENTRIES) {
                return split();
            }
            return null;
        }

        @Override
        void search(Rectangle range, List<T> results) {
            if (!mbr.intersects(range))
                return;
            for (Entry<T> e : entries) {
                if (range.intersects(e.mbr)) {
                    results.add(e.object);
                }
            }
        }

        @Override
        void recalculateMBR() {
            mbr = null;
            for (Entry<T> e : entries) {
                mbr = (mbr == null) ? e.mbr : combine(mbr, e.mbr);
            }
        }

        @Override
        List<Entry<T>> getEntries() {
            return entries;
        }

        private Split split() {
            // Linear split
            int n = entries.size();
            // pick seeds
            Entry<T>[] seed = pickSeeds(entries);
            LeafNode left = new LeafNode();
            LeafNode right = new LeafNode();
            left.addEntry(seed[0]);
            right.addEntry(seed[1]);

            // distribute rest
            for (Entry<T> e : entries) {
                if (e == seed[0] || e == seed[1])
                    continue;
                if (left.entries.size() + (n - left.entries.size() - right.entries.size()) == MIN_ENTRIES) {
                    left.addEntry(e);
                } else if (right.entries.size() + (n - left.entries.size() - right.entries.size()) == MIN_ENTRIES) {
                    right.addEntry(e);
                } else {
                    float enlargeLeft = enlargement(left.mbr, e.mbr);
                    float enlargeRight = enlargement(right.mbr, e.mbr);
                    if (enlargeLeft < enlargeRight)
                        left.addEntry(e);
                    else if (enlargeRight < enlargeLeft)
                        right.addEntry(e);
                    else {
                        if (area(left.mbr) < area(right.mbr))
                            left.addEntry(e);
                        else
                            right.addEntry(e);
                    }
                }
            }
            return new Split(left, right);
        }

        private void addEntry(Entry<T> e) {
            entries.add(e);
            mbr = (mbr == null) ? e.mbr : combine(mbr, e.mbr);
        }
    }

    private class InternalNode extends Node {
        List<Node> children = new ArrayList<>();

        @Override
        Split insert(Entry<T> entry) {
            if (children.isEmpty()) {
                // No child yet: create a leaf to hold this entry
                LeafNode leaf = new LeafNode();
                leaf.addEntry(entry);
                this.addChild(leaf);
                this.mbr = combine(this.mbr, leaf.mbr);
                return null;
            }
            Node best = null;
            float bestEnl = Float.MAX_VALUE;
            for (Node c : children) {
                float enl = enlargement(c.mbr, entry.mbr);
                if (best == null || enl < bestEnl || (enl == bestEnl && area(c.mbr) < area(best.mbr))) {
                    bestEnl = enl;
                    best = c;
                }
            }
            Split split = best.insert(entry);
            if (split != null) {
                children.remove(best);
                addChild(split.left);
                addChild(split.right);
            }
            recalculateMBR();
            if (children.size() > MAX_ENTRIES) {
                return splitInternal();
            }
            return null;
        }

        @Override
        void search(Rectangle range, List<T> results) {
            if (!mbr.intersects(range))
                return;
            for (Node c : children) {
                c.search(range, results);
            }
        }

        @Override
        void recalculateMBR() {
            mbr = null;
            for (Node c : children) {
                mbr = (mbr == null) ? c.mbr : combine(mbr, c.mbr);
            }
        }

        @Override
        List<Entry<T>> getEntries() {
            return new ArrayList<>(); // Internal nodes do not have entries
        }

        private Split splitInternal() {
            List<Node> old = new ArrayList<>(children);
            children.clear();
            int n = old.size();
            Node[] seeds = pickSeedsNode(old);
            InternalNode left = new InternalNode();
            InternalNode right = new InternalNode();
            left.addChild(seeds[0]);
            right.addChild(seeds[1]);
            for (Node c : old) {
                if (c == seeds[0] || c == seeds[1])
                    continue;
                if (left.children.size() + (n - left.children.size() - right.children.size()) == MIN_ENTRIES) {
                    left.addChild(c);
                } else if (right.children.size() + (n - left.children.size() - right.children.size()) == MIN_ENTRIES) {
                    right.addChild(c);
                } else {
                    float dl = enlargement(left.mbr, c.mbr);
                    float dr = enlargement(right.mbr, c.mbr);
                    if (dl < dr)
                        left.addChild(c);
                    else if (dr < dl)
                        right.addChild(c);
                    else if (area(left.mbr) < area(right.mbr))
                        left.addChild(c);
                    else
                        right.addChild(c);
                }
            }
            return new Split(left, right);
        }

        private void addChild(Node c) {
            if (c == null)
                return;
            children.add(c);
            mbr = (mbr == null) ? c.mbr : combine(mbr, c.mbr);
        }
    }

    private static class Entry<T extends SpatialObject> {
        Rectangle mbr;
        T object;

        Entry(T object) {
            this.mbr = object.getAABB();
            this.object = object;
        }
    }

    private class Split {
        Node left;
        Node right;

        Split(Node left, Node right) {
            this.left = left;
            this.right = right;
        }
    }

    /* Helpers */
    private Rectangle combine(Rectangle a, Rectangle b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        float x1 = Math.min(a.x(), b.x());
        float y1 = Math.min(a.y(), b.y());
        float x2 = Math.max(a.x() + a.width(), b.x() + b.width());
        float y2 = Math.max(a.y() + a.height(), b.y() + b.height());
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    private float area(Rectangle r) {
        return r == null ? 0 : r.width() * r.height();
    }

    private float enlargement(Rectangle r, Rectangle toAdd) {
        Rectangle combined = combine(r, toAdd);
        return area(combined) - area(r);
    }

    @SuppressWarnings("unchecked")
    private Entry<T>[] pickSeeds(List<Entry<T>> entries) {
        Entry<T> seed1 = null, seed2 = null;
        float worst = -1;
        int n = entries.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Rectangle e1 = entries.get(i).mbr;
                Rectangle e2 = entries.get(j).mbr;
                float waste = area(combine(e1, e2)) - area(e1) - area(e2);
                if (waste > worst) {
                    worst = waste;
                    seed1 = entries.get(i);
                    seed2 = entries.get(j);
                }
            }
        }
        return (Entry<T>[]) new Entry[] { seed1, seed2 };
    }

    @SuppressWarnings("unchecked")
    private Node[] pickSeedsNode(List<Node> nodes) {
        Node seed1 = null, seed2 = null;
        float worst = -1;
        int n = nodes.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Rectangle m1 = nodes.get(i).mbr;
                Rectangle m2 = nodes.get(j).mbr;
                float waste = area(combine(m1, m2)) - area(m1) - area(m2);
                if (waste > worst) {
                    worst = waste;
                    seed1 = nodes.get(i);
                    seed2 = nodes.get(j);
                }
            }
        }
        Node[] result = new RTree.Node[2]; // Unsafe cast, but safe in context
        result[0] = seed1;
        result[1] = seed2;
        return result;
    }

    @Override
    public void draw(PApplet p) {
        p.noFill();
        drawNode(p, root, 0);
    }

    private void drawNode(PApplet p, Node node, int depth) {
        for (Entry<T> entry : node.getEntries()) {
            entry.object.draw(p);
        }

        p.stroke(255, 0, 0);

        Rectangle r = node.mbr;
        p.rect(r.x(), r.y(), r.width(), r.height());

        if (node instanceof InternalNode) {
            for (Node child : ((InternalNode) node).children) {
                drawNode(p, child, depth + 1);
            }
        }
    }
}
