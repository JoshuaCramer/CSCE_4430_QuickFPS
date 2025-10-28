import java.util.ArrayList;

public class KDNode {
    public ArrayList<Point> points;
    public int pointLeft;
    public int pointRight;
    public int idx;

    public Interval[] bbox = { new Interval(), new Interval(), new Interval() };

    public ArrayList<Point> waitpoints  = new ArrayList<>();
    public ArrayList<Point> delaypoints = new ArrayList<>();
    public Point max_point = new Point();

    public KDNode left;
    public KDNode right;

    // Placeholder
    public void init(Point ref) { }
}
