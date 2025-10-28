import java.util.ArrayList;

//this class is mostly just a placeholder for now
public class KDLineTree extends KDTreeBase {
    KDLineTree(ArrayList<Point> data, int pointSize, ArrayList<Point> samplePoints) {
        super(data, pointSize, samplePoints);
    }
    public void addNode(KDNode p) {}; //p is a pointer
    public boolean leftNode(int high, int count) {
        //placeholder
        return false;
    };
    public Point max_point() {
        //placeholder
        return new Point();
    };
    public void update_distance(Point ref_point) {};
    public void sample(int sample_num) {};
}
