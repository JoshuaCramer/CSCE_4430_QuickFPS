import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class KDNode {
    public ArrayList<Point> points = new ArrayList<Point>();
    public int pointLeft = 0;
    public int pointRight = 0;
    public int idx;

    Interval[] bbox;
    ArrayList<Point> waitpoints = new ArrayList<Point>();
    ArrayList<Point> delaypoints = new ArrayList<Point>();
    Point max_point;
    KDNode left;
    KDNode right;

    KDNode() {}

    KDNode(KDNode a) { //a is reference variable
        this.points = a.points;
        this.pointLeft = a.pointLeft;
        this.pointRight = a.pointRight;
        this.delaypoints = a.delaypoints;
        this.max_point = a.max_point;
        this.left = a.left;
        this.right = a.right;
    }

    void init(Point ref) {  //ref is reference variable
        waitpoints.clear();
        delaypoints.clear();
        if(this.left != null && this.right != null) {
            this.left.init(ref);
            this.right.init(ref);
            updateMaxPoint(this.left.max_point, this.right.max_point);
        } else {
            float dis;
            float maxdis = -1.0f;
            for(int i = pointLeft; i < pointRight; i++) {
                dis = points.get(i).updatedistance(ref);
                if (dis > maxdis) {
                    maxdis = dis;
                    max_point = points.get(i);
                }
            }
        }
    }

    void updateMaxPoint(Point lpoint, Point rpoint) {   //both are reference variables
        if(lpoint.dis > rpoint.dis) this.max_point = lpoint;
        else this.max_point = rpoint;
    }

    float bound_distance(Point ref_point) { //ref_point is reference variable
        float bound_dis = 0f;
        float dim_distance;
        for(int cur_dim = 0; cur_dim < 3; cur_dim++) {
            dim_distance = 0;
            if(ref_point.get(cur_dim) > bbox[cur_dim].high) 
                dim_distance = ref_point.get(cur_dim) - bbox[cur_dim].high;
            if(ref_point.get(cur_dim) < bbox[cur_dim].low) 
                dim_distance = bbox[cur_dim].low - ref_point.get(cur_dim);
            bound_dis += Utils.pow2(dim_distance);
        }
        return bound_dis;
    }

    void send_delay_point(Point point) {    //point is reference variable
        this.waitpoints.addLast(point);
    }

    void logging(int idx, int pointSize){
        System.out.println("Calculate Bucket: " + idx + " size: " + pointSize);
    }

    void update_distance(AtomicInteger memory_ops, AtomicInteger mult_ops) {  //both are reference variables, and modified
        for(Point ref_point : this.waitpoints) {
            float lastmax_distance = this.max_point.dis;
            float cur_distance = this.max_point.distance(ref_point);
            mult_ops.incrementAndGet();
            if (cur_distance > lastmax_distance) {
                float boundary_distance = bound_distance(ref_point);
                mult_ops.incrementAndGet();
                if (boundary_distance < lastmax_distance) {
                    this.delaypoints.addLast(ref_point);
                }
                //ifdef NOMAPPING

                //ifndef MERGESIZE
                int MERGESIZE = 4;
                if(this.delaypoints.size() >= MERGESIZE) {
                    float dis;
                    float maxdis;
                    for(Point delay_point : delaypoints) {
                        maxdis = -1f;
                        for(int i = pointLeft; i < pointRight; i++) {
                            dis = points.get(i).updatedistance(delay_point);
                            if(dis > maxdis) {
                                maxdis = dis;
                                max_point = points.get(i);
                            }
                        }
                    }
                    this.delaypoints.clear();
                }
                //endif NOMAPPING

            } else {
                if(this.right != null && this.left != null) {
                    if(!delaypoints.isEmpty()) {
                        for(Point delay_point : delaypoints) {
                            this.left.send_delay_point(delay_point);
                            this.right.send_delay_point(delay_point);
                        }
                        delaypoints.clear();
                    }

                    this.left.send_delay_point(ref_point);
                    this.left.update_distance(memory_ops, mult_ops);

                    this.right.send_delay_point(ref_point);
                    this.right.update_distance(memory_ops, mult_ops);

                    updateMaxPoint(this.left.max_point, this.right.max_point);
                } else {
                    float dis;
                    float maxdis;
                    this.delaypoints.addLast(ref_point);
                    for (Point delay_point : delaypoints) {
                        maxdis = -1;
                        for(int i = pointLeft; i < pointRight; i++){
                            dis = points.get(i).updatedistance(delay_point);
                            if (dis > maxdis) {
                                maxdis = dis;
                                max_point = points.get(i);
                            }
                        }
                    }
                    mult_ops.getAndAdd(delaypoints.size() * (pointRight - pointLeft));
                    memory_ops.getAndAdd(pointRight - pointLeft);
                    this.delaypoints.clear();
                }
            }
        }
        this.waitpoints.clear();
    }

    void reset() {
        for(int i = pointLeft; i < pointRight; i++) {
            points.get(i).reset();
        }
        this.waitpoints.clear();
        this.delaypoints.clear();
        this.max_point.reset();
        if(this.left != null && this.right != null) {
            this.left.reset();
            this.right.reset();
        }
    }

    int size() {
        if(this.left != null && this.right != null) {
            return this.left.size() + this.right.size();
        }
        return (pointRight - pointLeft);
    }
}
