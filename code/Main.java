public class Main {
    public static void main(String[] args){
        Point a = new Point(0f, 0f, 0f, 1e20f, 1);
        Point b = new Point(1f, 2f, 5f, 1e20f, 2);

        System.out.println("Point A: " + a);
        System.out.println("Point B: " + b);

        float dist = a.distance(b);
        System.out.println("Squared Distance between A and B: " + dist);

        a.updatedistance(b);
        System.out.println("A after updateDistance: " + a);

        a.reset();
        System.out.println("A after reset: " + a);
    }
}