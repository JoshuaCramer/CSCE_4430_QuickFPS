// Point.java
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Point implements Comparable<Point> {
    // x, y, z
    public final float[] pos = new float[3];
    public float dis;
    public int id;

    // Default constructor: dis = 1e20, pos = {0,0,0}, id = 0
    public Point() {
        this.pos[0] = 0f; this.pos[1] = 0f; this.pos[2] = 0f;
        this.dis = 1e20f;
        this.id = 0;
    }

    // Full constructor
    public Point(float x, float y, float z, float dis, int id) {
        this.pos[0] = x; this.pos[1] = y; this.pos[2] = z;
        this.dis = dis;
        this.id = id;
    }

    public Point(Point obj) {
        this(obj.pos[0], obj.pos[1], obj.pos[2], obj.dis, obj.id);
    }

    public float get(int i) {
        return pos[i];
    }

    @Override
    public int compareTo(Point other) {
        return Float.compare(this.dis, other.dis);
    }

    public float distance(Point b) {
        float dx = Utils.pow2(this.pos[0] - b.pos[0]);
        float dy = Utils.pow2(this.pos[1] - b.pos[1]);
        float dz = Utils.pow2(this.pos[2] - b.pos[2]);
        return dx + dy + dz;
    }

    public float updatedistance(Point ref) {
        this.dis = Math.min(this.dis, distance(ref));
        return this.dis;
    }

    public float updateDistanceAndCount(Point ref, AtomicInteger count) {
        float temp = distance(ref);
        if (temp < this.dis) {
            this.dis = temp;
            if (count != null) count.incrementAndGet();
        }
        return this.dis;
    }


    public void reset() {
        this.dis = 1e20f;
    }

    @Override
    public String toString() {
        return "Point: [" + pos[0] + ", " + pos[1] + ", " + pos[2] + "]  id:" + id + " dist:" + dis;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point A = (Point) o;
        return Float.compare(pos[0], A.pos[0]) == 0
            && Float.compare(pos[1], A.pos[1]) == 0
            && Float.compare(pos[2], A.pos[2]) == 0
            && Float.compare(dis, A.dis) == 0
            && id == A.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos[0], pos[1], pos[2], dis, id);
    }
}
