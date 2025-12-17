// TestComplex.java
import java.util.List;
import java.util.ArrayList;

public class TestComplex {
    private String userName = "John";
    private List<String> items = new ArrayList<>();
    private int itemCount = 0;

    public void addItem(String item) {
        items.add(item);
        itemCount++;
        System.out.println("Added: " + item);
    }

    public void printAll() {
        for (String item : items) {
            System.out.println("Item: " + item);
        }
        System.out.println("Total: " + itemCount);
    }

    public static void main(String[] args) {
        TestComplex app = new TestComplex();
        app.addItem("First");
        app.addItem("Second");
        app.printAll();
    }
}