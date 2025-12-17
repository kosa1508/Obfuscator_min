
public class HelloWorld {
    private String message = "Hello World!";
    private int counter = 0;

    public void printMessage() {
        System.out.println(message);
        counter++;
        System.out.println("Counter: " + counter);
    }

    public void myMethod(String param) {
        String localVar = "Local: " + param;
        System.out.println(localVar);
    }

    public static void main(String[] args) {
        HelloWorld obj = new HelloWorld();
        obj.printMessage();
        obj.myMethod("Test");

        for (int i = 0; i < 3; i++) {
            System.out.println("Loop: " + i);
        }
    }
}