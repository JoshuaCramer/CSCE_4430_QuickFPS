public class Main {
    public static void main(String[] args){
        System.out.println("Hello, World!");
        float number = 3.0f;
        float result = Utils.pow2(number);
        Interval test = new Interval();
        test.high = 5;
        test.low = 0;
        System.out.println("The high is " + test.high + " and the low is " + test.low);
        System.out.println("The square of " + number + " is " + result);
    }
}