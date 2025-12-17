package test.java;

public class SimpleClass {
    private String name;
    private int count;

    public SimpleClass(String name) {
        this.name = name;
        this.count = 0;
    }

    public void increment() {
        count++;
    }

    public void printInfo() {
        System.out.println("Name: " + name + ", Count: " + count);
    }

    public static void main(String[] args) {
        SimpleClass obj = new SimpleClass("Test");
        obj.increment();
        obj.printInfo();
    }
}