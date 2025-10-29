import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
//the original code's version is not abstract but I am pretty sure this class is not supposed to be used on its own
abstract public class KDTreeBase {
    private int DIM = 3;    //replaces a macro in the original code

    public Integer pointSize;
    public AtomicInteger memory_ops = new AtomicInteger(0);
    public AtomicInteger mult_ops = new AtomicInteger(0);
    public ArrayList<Point> sample_points = new ArrayList<Point>();
    public KDNode root_ = null;
    public ArrayList<Point> points_ = new ArrayList<Point>();

    public KDTreeBase(ArrayList<Point> data, int pointSize, ArrayList<Point> samplePoints) {
        this.pointSize = pointSize;
        this.sample_points = samplePoints;
        this.points_ = data;
    }

    public void buildKDTree() {
        Interval[] bbox = new Interval[DIM];
        int left = 0;
        int right = pointSize;
        computeBoundingBox(left, right, bbox);
        this.root_ = divideTree(left, right, bbox, 0);
    }

    KDNode get_root() {return this.root_; }

    KDNode divideTree(int left, int right, Interval[] bbox_ptr, int curr_high) {    //bbox_ptr is reference variable
        KDNode node = new KDNode();
        //copy bounding box array
        for(int i = 0; i < DIM; i++) {
            node.bbox[i] = bbox_ptr[i];
        }
        
        int count = right - left;
        if(leftNode(curr_high, count)) {
            node.pointLeft = left;
            node.pointRight = right;
            node.points = this.points_;
            addNode(node);
            return node;
        } else {
            Integer split_dim = 0;
            Float split_val = 0.0f;
            findSplitDim(split_dim, bbox_ptr);
            qSelectMedian(split_dim, left, right, split_val);

            Integer split_delta = 0;
            planeSplit(left, right, split_dim, split_val, split_delta);

            Interval[] bbox_cur = new Interval[3];
            computeBoundingBox(left, left + split_delta, bbox_cur);
            node.left = divideTree(left, left + split_delta, bbox_cur, curr_high + 1);
            computeBoundingBox(left + split_delta, right, bbox_cur);
            node.right = divideTree(left + split_delta, right, bbox_cur, curr_high + 1);
            return node;
        }
    }
    
    public void planeSplit(int left, int right, int split_dim, float split_val, Integer lim1) { //lim1 is reference variable
        int start = left;
        int end = right + 1;

        while(true) {
            while(start <= end && points_.get(start).pos[split_dim] < split_val) {
                ++start;
            }     
            while(start <= end && points_.get(end).pos[split_dim] >= split_val) {
                --end;
            }
                
            if(start > end) break;
            Collections.swap(points_, start, end);
            ++start;
            --end;
        }
        lim1 = start - left;
        if(start == left) lim1 = 1;
        if(start == right) lim1 = (right - left - 1);
    }

    public void qSelectMedian(int dim, int left, int right, Float median_value) {   //median_value is reference variable
        float sum = 0;
        for (int i = left; i < right; i++) {
            sum += points_.get(i).pos[dim];
        }
        median_value = sum / (right - left);
    }

    public void findSplitDim(Integer best_dim, Interval[] bbox_ptr) {   //both are reference variables
        float min_, max_;
        float span = 0.0f;

        for(int cur_dim = 0; cur_dim < DIM; cur_dim++) {
            min_ = bbox_ptr[cur_dim].low;
            max_ = bbox_ptr[cur_dim].high;

            if ((max_ - min_) > span ) {
                best_dim = cur_dim;
                span = (max_ - min_);
            }
        }
    }

    void computeBoundingBox(int left, int right, Interval[] bbox_ptr) { //bbox_ptr is reference variable
        float min_val_0 = points_.get(left).get(0);
        float max_val_0 = points_.get(left).get(0);
        float min_val_1 = points_.get(left).get(1);
        float max_val_1 = points_.get(left).get(1);
        float min_val_2 = points_.get(left).get(2);
        float max_val_2 = points_.get(left).get(2);

        float val_0, val_1, val_2;
        for(int i = left + 1; i < right; ++i) {
            float[] pos = points_.get(i).pos;
            val_0 = pos[0];
            val_1 = pos[1];
            val_2 = pos[2];

            if (val_0 < min_val_0) min_val_0 = val_0;
            if (val_0> max_val_0) max_val_0 = val_0;

            if (val_1 < min_val_1) min_val_1 = val_1;
            if (val_1> max_val_1) max_val_1 = val_1;

            if (val_2 < min_val_2) min_val_2 = val_2;
            if (val_2> max_val_2) max_val_2 = val_2;
        }
        bbox_ptr[0].high = max_val_0;
        bbox_ptr[0].low = min_val_0;

        bbox_ptr[1].high = max_val_1;
        bbox_ptr[1].low = min_val_1;

        bbox_ptr[2].high = max_val_2;
        bbox_ptr[2].low = min_val_2;
    }

    public void computeMinMax(int left, int right, int dim, Interval bound) {   //bound is reference variable
        float min_val = points_.get(left).get(dim);
        float max_val = points_.get(left).get(dim);
        for (int i = left + 1; i < right; ++i) {

            float val = points_.get(i).get(dim);

            if (val < min_val) min_val = val;
            if (val > max_val) max_val = val;
        }
        bound.high = max_val;
        bound.low = min_val;
    }

    public void init(Point ref) {   //ref is reference variable
        sample_points.set(0, ref);
        root_.init(ref);
    }

    public void cout_sample(int sampleSize) {
        for(int i = 0; i < sampleSize; i++) {
            System.out.println(sample_points.get(i));
        }
    }

    public int verify(int sampleSize) {
        int idsum = 0;
        for(int i = 0; i < sampleSize; i++) {
            idsum += sample_points.get(i).id;
        }
        return idsum;
    }

    abstract public void addNode(KDNode p); //p is a pointer
    abstract public boolean leftNode(int high, int count);
    abstract public Point max_point();
    abstract public void update_distance(Point ref_point);
    abstract public void sample(int sample_num);
}
