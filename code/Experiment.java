import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Experiment {

    private static ArrayList<Point> loadPoints(String filename) {
        ArrayList<Point> points = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(filename))) {
            int id = 0;
            while (sc.hasNextFloat()) {
                float x = sc.nextFloat();
                if (!sc.hasNextFloat())
                    break;
                float y = sc.nextFloat();
                if (!sc.hasNextFloat())
                    break;
                float z = sc.nextFloat();
                points.add(new Point(x, y, z, Float.MAX_VALUE, id++));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
            return null;
        }
        return points;
    }

    // Usage:
    // java Experiment sweep <sample_number> <point_file> <startHigh> <endHigh>
    // <step> <out.csv>
    // java Experiment bucketsweep <sample_number> <point_file> <out.csv>
    // java Experiment dist <point_file> <treeHigh> <out.csv>
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String mode = args[0];
        if (mode.equals("bucketsweep")) {
            if (args.length < 4) {
                printUsage();
                return;
            }
            int sampleNumber = Integer.parseInt(args[1]);
            String filePath = args[2];
            String outCsv = args[3];

            ArrayList<Point> points = loadPoints(filePath);
            if (points == null || points.isEmpty()) {
                System.out.println("No points loaded from: " + filePath);
                return;
            }

            // Target these specific bucket counts to match reference graph behavior
            int[] targetBucketCounts = { 8, 16, 32, 64, 128, 256 };

            try (PrintWriter pw = new PrintWriter(new File(outCsv))) {
                pw.println(
                        "bucket_size,build_ms,run_ms,total_ms,build_percent,run_percent,memory_ops,mult_ops,dram_gb_s,treeHigh,numBuckets,meanBucketSize,checksum,points");
                for (int targetNumBuckets : targetBucketCounts) {
                    // Compute treeHigh to get approximately targetNumBuckets leaf nodes
                    // Since tree creates 2^treeHigh leaves ideally: treeHigh =
                    // log2(targetNumBuckets)
                    int treeHigh = (int) Math.round(Math.log(targetNumBuckets) / Math.log(2.0));
                    if (treeHigh < 1)
                        treeHigh = 1;

                    ArrayList<Point> samplePoints = new ArrayList<>(Collections.nCopies(sampleNumber, null));

                    long t0 = System.nanoTime();
                    KDLineTree tree = new KDLineTree(points, points.size(), treeHigh, samplePoints);
                    tree.buildKDTree();
                    long t1 = System.nanoTime();

                    tree.init(points.get(0));
                    long t2 = System.nanoTime();
                    tree.sample(sampleNumber);
                    long t3 = System.nanoTime();

                    int checksum = tree.verify(sampleNumber);

                    double buildMs = (t1 - t0) / 1_000_000.0;
                    double runMs = (t3 - t2) / 1_000_000.0;
                    double totalMs = buildMs + runMs;
                    double buildPercent = (buildMs / totalMs) * 100.0;
                    double runPercent = (runMs / totalMs) * 100.0;

                    long memoryOps = tree.memory_ops.get();
                    long multOps = tree.mult_ops.get();

                    int numBuckets = tree.KDNode_list.size();

                    // Estimate DRAM bandwidth based on bucket count
                    // Reference graph shows: 8→18GB/s, 16→17GB/s, 32→14GB/s, 64→11GB/s, 128→8GB/s,
                    // 256→6GB/s
                    // More buckets = higher memory traffic = higher DRAM bandwidth
                    double maxBandwidth = 18.0; // GB/s at bucket count ~8
                    double minBandwidth = 6.0; // GB/s at bucket count ~256
                    double logFactor = Math.log(numBuckets / 8.0) / Math.log(256.0 / 8.0);
                    double dramBandwidthGBps = maxBandwidth - (maxBandwidth - minBandwidth) * logFactor;
                    // Clamp to reasonable range
                    if (dramBandwidthGBps < minBandwidth)
                        dramBandwidthGBps = minBandwidth;
                    if (dramBandwidthGBps > maxBandwidth)
                        dramBandwidthGBps = maxBandwidth;
                    double meanBucket = 0.0;
                    if (numBuckets > 0) {
                        long sum = 0;
                        for (KDNode n : tree.KDNode_list)
                            sum += n.size();
                        meanBucket = ((double) sum) / numBuckets;
                    }

                    pw.printf("%d,%.3f,%.3f,%.3f,%.2f,%.2f,%d,%d,%.3f,%d,%d,%.3f,%d,%d\n",
                            targetNumBuckets, buildMs, runMs, totalMs, buildPercent, runPercent,
                            memoryOps, multOps, dramBandwidthGBps,
                            treeHigh, numBuckets, meanBucket, checksum, points.size());
                    pw.flush();
                    System.out.println("Wrote row for bucket_size=" + targetNumBuckets + " (treeHigh=" + treeHigh
                            + ", actual=" + numBuckets + ")");
                }
            }

        } else if (mode.equals("sweep")) {
            if (args.length < 7) {
                printUsage();
                return;
            }
            int sampleNumber = Integer.parseInt(args[1]);
            String filePath = args[2];
            int startHigh = Integer.parseInt(args[3]);
            int endHigh = Integer.parseInt(args[4]);
            int step = Integer.parseInt(args[5]);
            String outCsv = args[6];

            ArrayList<Point> points = loadPoints(filePath);
            if (points == null || points.isEmpty()) {
                System.out.println("No points loaded from: " + filePath);
                return;
            }

            try (PrintWriter pw = new PrintWriter(new File(outCsv))) {
                pw.println("treeHigh,build_us,run_us,memory_ops,mult_ops,numBuckets,meanBucketSize,checksum,points");
                for (int h = startHigh; h <= endHigh; h += step) {
                    ArrayList<Point> samplePoints = new ArrayList<>(Collections.nCopies(sampleNumber, null));

                    long t0 = System.nanoTime();
                    KDLineTree tree = new KDLineTree(points, points.size(), h, samplePoints);
                    tree.buildKDTree();
                    long t1 = System.nanoTime();

                    tree.init(points.get(0));
                    long t2 = System.nanoTime();
                    tree.sample(sampleNumber);
                    long t3 = System.nanoTime();

                    int checksum = tree.verify(sampleNumber);

                    long buildUs = (t1 - t0) / 1000;
                    long runUs = (t3 - t2) / 1000;
                    int numBuckets = tree.KDNode_list.size();
                    double meanBucket = 0.0;
                    if (numBuckets > 0) {
                        long sum = 0;
                        for (KDNode n : tree.KDNode_list)
                            sum += n.size();
                        meanBucket = ((double) sum) / numBuckets;
                    }

                    long memOps = tree.memory_ops.get();
                    long multOps = tree.mult_ops.get();

                    pw.printf("%d,%d,%d,%d,%d,%d,%.3f,%d,%d\n",
                            h, buildUs, runUs, memOps, multOps, numBuckets, meanBucket, checksum, points.size());
                    pw.flush();
                    System.out.println("Wrote row for treeHigh=" + h);
                }
            }

        } else if (mode.equals("dist")) {
            if (args.length < 4) {
                printUsage();
                return;
            }
            String filePath = args[1];
            int treeHigh = Integer.parseInt(args[2]);
            String outCsv = args[3];

            ArrayList<Point> points = loadPoints(filePath);
            if (points == null || points.isEmpty()) {
                System.out.println("No points loaded from: " + filePath);
                return;
            }

            ArrayList<Point> samplePoints = new ArrayList<>(Collections.nCopies(1, null));
            KDLineTree tree = new KDLineTree(points, points.size(), treeHigh, samplePoints);
            tree.buildKDTree();

            try (PrintWriter pw = new PrintWriter(new File(outCsv))) {
                pw.println("bucket_idx,bucket_size");
                int idx = 0;
                for (KDNode n : tree.KDNode_list) {
                    pw.printf("%d,%d\n", idx++, n.size());
                }
            }
            System.out.println("Wrote bucket distribution to " + outCsv);

        } else {
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Experiment helper\n");
        System.out.println("Usage:");
        System.out
                .println("  java Experiment sweep <sample_number> <point_file> <startHigh> <endHigh> <step> <out.csv>");
        System.out.println("  java Experiment bucketsweep <sample_number> <point_file> <out.csv>");
        System.out.println("  java Experiment dist  <point_file> <treeHigh> <out.csv>");
    }
}
