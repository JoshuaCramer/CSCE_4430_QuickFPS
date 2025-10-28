import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Random;

public class Main {
    public static void main(String[] args){
        // Point class test
        Point a = new Point(0f, 0f, 0f, 1e20f, 1);
        Point b = new Point(1f, 2f, 5f, 1e20f, 2);

        System.out.println("Point A: " + a);
        System.out.println("Point B: " + b);

        float dist = a.distance(b);
        System.out.println("Squared Distance between A and B: " + dist);

        a.updatedistance(b);
        System.out.println("A after updateDistance: " + a);

        a.reset();
        System.out.println("A after reset: " + a);

        // KDTreeBase class test
        // 1) Generate some random points
        ArrayList<Point> pts = new ArrayList<>();
        Random r = new Random(42);
        for (int i = 0; i < 40; i++) {
            pts.add(new Point(
                r.nextFloat() * 100f,
                r.nextFloat() * 100f,
                r.nextFloat() * 100f,
                1e20f, i
            ));
        }

        // sample_points can be empty placeholders for now
        ArrayList<Point> samples = new ArrayList<>(Collections.nCopies(4, new Point()));

        // 2) Build the tree
        KDLineTree tree = new KDLineTree(pts, pts.size(), samples);
        tree.buildKDTree();

        // 3) Print quick summary
        KDNode root = tree.get_root();
        System.out.println("KD-tree built!");
        printBBox("Root bbox", root.bbox);

        // 4) Walk the tree and check leaf ranges + bbox correctness
        int leaves = 0, nodes = 0, covered = 0;
        Deque<KDNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            KDNode n = stack.pop();
            nodes++;
            if (n.left == null && n.right == null) {
                leaves++;
                covered += (n.pointRight - n.pointLeft);
                System.out.printf("Leaf range: [%d, %d)%n", n.pointLeft, n.pointRight);
                // Verify bbox matches data for this leaf
                verifyBBoxAgainstData(pts, n);
            } else {
                if (n.right != null) stack.push(n.right);
                if (n.left  != null) stack.push(n.left);
            }
        }

        // 5) Sanity checks
        System.out.printf("Nodes: %d, Leaves: %d, Covered: %d of %d%n",
                nodes, leaves, covered, pts.size());
        if (covered != pts.size()) {
            System.err.println("ERROR: Leaf ranges do not cover all points!");
        } else {
            System.out.println("Coverage OK ✅");
        }
    }

    private static void printBBox(String label, Interval[] b) {
        System.out.printf("%s: x[%.2f, %.2f] y[%.2f, %.2f] z[%.2f, %.2f]%n",
            label, b[0].low, b[0].high, b[1].low, b[1].high, b[2].low, b[2].high);
    }

    // Recompute min/max from the points in this node’s range and compare with node.bbox
    private static void verifyBBoxAgainstData(ArrayList<Point> pts, KDNode n) {
        float min0 = Float.POSITIVE_INFINITY, max0 = Float.NEGATIVE_INFINITY;
        float min1 = Float.POSITIVE_INFINITY, max1 = Float.NEGATIVE_INFINITY;
        float min2 = Float.POSITIVE_INFINITY, max2 = Float.NEGATIVE_INFINITY;

        for (int i = n.pointLeft; i < n.pointRight; i++) {
            float[] p = pts.get(i).pos;
            if (p[0] < min0) min0 = p[0]; if (p[0] > max0) max0 = p[0];
            if (p[1] < min1) min1 = p[1]; if (p[1] > max1) max1 = p[1];
            if (p[2] < min2) min2 = p[2]; if (p[2] > max2) max2 = p[2];
        }

        boolean ok =
            approxEq(min0, n.bbox[0].low)  && approxEq(max0, n.bbox[0].high) &&
            approxEq(min1, n.bbox[1].low)  && approxEq(max1, n.bbox[1].high) &&
            approxEq(min2, n.bbox[2].low)  && approxEq(max2, n.bbox[2].high);

        if (!ok) {
            System.err.printf("BBox mismatch in leaf [%d,%d):%n", n.pointLeft, n.pointRight);
            printBBox("  node", n.bbox);
            System.err.printf("  data: x[%.2f, %.2f] y[%.2f, %.2f] z[%.2f, %.2f]%n",
                min0, max0, min1, max1, min2, max2);
        }
    }

    private static boolean approxEq(float a, float b) {
        return Math.abs(a - b) <= 1e-5f;
    }
    
}