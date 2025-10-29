import java.util.ArrayList;

public class KDTree extends KDTreeBase {

    public KDTree(ArrayList<Point> data, int pointSize, ArrayList<Point> samplePoints) {
        super(data, pointSize, samplePoints);
    }

    public Point max_point() {
        return this.root_.max_point;
    }

    public void update_distance(Point ref_point) {
        this.root_.send_delay_point(ref_point);
        this.root_.update_distance(memory_ops, mult_ops);
    };

    public void sample(int sample_num) {
        Point ref_point;

        //if metrics enabled
        //int pre_memory_ops = 0
        int i;
        for(i = 1; i < sample_num; i++) {
            ref_point = max_point();
            sample_points.set(i, ref_point);
            update_distance(ref_point);
            // if logging enabled
            // System.out.println("Next iteration\n");
            // if metrics enabled
            // System.out.println(i + ":" + memory_ops - pre_memory_ops);
        }
    };

    public boolean leftNode(int high, int count) {
        return count == 1;
    }

    public void addNode(KDNode p) {
        // no op for some reason
    }
}
