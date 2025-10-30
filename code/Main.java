import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Main <sample_number> <point_file> [treeHigh]");
            return;
        }

        final int sampleNumber = Integer.parseInt(args[0]);
        final String filePath = args[1];
        final int treeHigh = (args.length >= 3) ? Integer.parseInt(args[2]) : 6;

        ArrayList<Point> points = loadPoints(filePath);
        if (points == null || points.isEmpty()) {
            System.out.println("No points loaded from: " + filePath);
            return;
        }

        runKDTreeAndReport(points, sampleNumber, filePath); // Run baseline KDTree
        System.out.println("-------------------------");
        runKDLineTreeAndReport(points, sampleNumber, treeHigh, filePath); // Run KDLineTree
    }

    private static ArrayList<Point> loadPoints(String filename) {
        ArrayList<Point> points = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(filename))) {
            int id = 0;
            while (sc.hasNextFloat()) {
                float x = sc.nextFloat();
                if (!sc.hasNextFloat()) break;
                float y = sc.nextFloat();
                if (!sc.hasNextFloat()) break;
                float z = sc.nextFloat();
                points.add(new Point(x, y, z, Float.MAX_VALUE, id++));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
            return null;
        }
        return points;
    }

    private static void runKDTreeAndReport(ArrayList<Point> points, int sampleNumber, String paramPath) {

        ArrayList<Point> samplePoints = new ArrayList<>(Collections.nCopies(sampleNumber, null));

        // Build
        long t0 = System.nanoTime();
        KDTree tree = new KDTree(points, points.size(), samplePoints);
        tree.buildKDTree();
        long t1 = System.nanoTime();

        // Sample
        tree.init(points.get(0));
        long t2 = System.nanoTime();
        tree.sample(sampleNumber);
        long t3 = System.nanoTime();

        int checksum = tree.verify(sampleNumber);

        printReport(
                "Baseline", /*high*/ -1,
                points.size(), sampleNumber,
                (t1 - t0), (t3 - t2),
                checksum, paramPath
        );
    }

    private static int runKDLineTreeAndReport( ArrayList<Point> points, int sampleNumber, int treeHigh, String paramPath) {

        ArrayList<Point> samplePoints = new ArrayList<>(Collections.nCopies(sampleNumber, null));

        // Build timing
        long t0 = System.nanoTime();
        KDLineTree tree = new KDLineTree(points, points.size(), treeHigh, samplePoints);
        tree.buildKDTree();
        long t1 = System.nanoTime();

        // Sampling timing
        tree.init(points.get(0));
        long t2 = System.nanoTime();
        tree.sample(sampleNumber);
        long t3 = System.nanoTime();

        // Verify/checksum
        int checksum = tree.verify(sampleNumber);

        printReport(
                "KDLineTree", treeHigh,
                points.size(), sampleNumber,
                (t1 - t0),          // build time (ns)
                (t3 - t2),          // run time (ns)
                checksum, paramPath
        );

        return checksum;
    }

    private static void printReport(
            String type, int high, int totalPoints, int sampleNumber,
            long buildTimeNs, long runTimeNs,
            long checksum, String paramPath) {

        double buildUs = buildTimeNs / 1_000.0;
        double runUs   = runTimeNs   / 1_000.0;

        System.out.println("Report:");
        System.out.printf("  Type   :%-10s High:%d%n", type, high);
        System.out.printf("  Points :%d%n", totalPoints);
        System.out.printf("  NPoint :%d%n", sampleNumber);
        System.out.printf("  RunTime:%.0fus%n", runUs);
        System.out.printf("  Build  :%.0fus%n", buildUs);
        System.out.printf("  Check  :%d%n", checksum);
        System.out.printf("  Param  :%s%n", paramPath);
        System.out.printf("  Timestamp:%s%n", java.time.ZonedDateTime.now().toString());
    }
}
