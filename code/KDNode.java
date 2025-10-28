import java.util.ArrayList;

//mostly a placeholder, feel free to replace anything
public class KDNode {
    public ArrayList<Point> points;
    public int pointLeft;
    public int pointRight;
    public int idx;

    Interval[] bbox;
    ArrayList<Point> waitpoints;
    ArrayList<Point> delaypoints;
    Point max_point;
    KDNode left;
    KDNode right;

    void init(Point ref) {  //ref is reference variable

    }

    void send_delay_point(Point point) {    //point is reference variable

    }

    void update_distance(int memory_ops, int mult_ops) {  //both are reference variables

    }
}
