import java.util.ArrayList;

//this class is mostly just a placeholder for now
public class KDLineTree extends KDTreeBase {

    public ArrayList<KDNode> KDNode_list = new ArrayList<KDNode>();
    public int high_;

    KDLineTree(ArrayList<Point> data, int pointSize, int treeHigh, ArrayList<Point> samplePoints) {
        super(data, pointSize, samplePoints);
        this.high_ = treeHigh;
        KDNode_list.clear();
    }

    public Point max_point() {
        Point tmpPoint = new Point();
        float max_distance = 0.0f;
        for(KDNode bucket : this.KDNode_list) {
            if(bucket.max_point.dis > max_distance) {
                max_distance = bucket.max_point.dis;
                tmpPoint = bucket.max_point;
            }
        }
        return tmpPoint;
    };

    public void update_distance(Point ref_point) {
        for(KDNode bucket : KDNode_list) {
            bucket.send_delay_point(ref_point);
            bucket.update_distance(memory_ops, mult_ops);
        }
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
        return high == high_ || count == 1;
    };

    public void addNode(KDNode p) { //p is a pointer
        int nodeIdx = KDNode_list.size();
        p.idx = nodeIdx;
        KDNode_list.addLast(p);
    };
}
