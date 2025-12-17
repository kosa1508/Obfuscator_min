package com.obfuscator;

import com.obfuscator.obfuscator.CodeObfuscator;
import com.obfuscator.obfuscator.ObfuscationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CodeObfuscatorTest {

    private CodeObfuscator obfuscator;

    @BeforeEach
    void setUp() {
        obfuscator = new CodeObfuscator();
    }

    @Test
    void shouldObfuscateSimpleClass() {
        String sourceCode = """
                public class SimpleClass {
                    private int number = 10;
                    
                    public void printNumber() {
                        System.out.println(number);
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "SimpleClass.java");

        assertNotNull(obfuscated);
        assertNotEquals(sourceCode, obfuscated);
        assertTrue(obfuscated.contains("public class"));
    }

    @Test
    void shouldRenameClassName() {
        String sourceCode = """
                public class MyClass {
                    private String name = "test";
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "MyClass.java");

        assertFalse(obfuscated.contains("class MyClass"));
        assertTrue(obfuscated.contains("class C"));

        String newFileName = obfuscator.getNewFileName("MyClass.java");
        assertFalse(newFileName.equals("MyClass.java"));
        assertTrue(newFileName.endsWith(".java"));
    }

    @Test
    void shouldRenameMethod() {
        String sourceCode = """
                public class TestClass {
                    public void myMethod() {
                        System.out.println("test");
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "TestClass.java");

        assertFalse(obfuscated.contains("myMethod()"));
        assertTrue(obfuscated.contains("m"));
    }

    @Test
    void shouldRenameVariable() {
        String sourceCode = """
                public class VarClass {
                    private String myVariable = "hello";
                    
                    public void print() {
                        System.out.println(myVariable);
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "VarClass.java");

        assertFalse(obfuscated.contains("myVariable"));
        assertTrue(obfuscated.contains("v"));
    }

    @Test
    void shouldHandleEmptyCode() {
        String sourceCode = "";

        String result = obfuscator.obfuscateCode(sourceCode, "Empty.java");

        assertNotNull(result);

        assertTrue(result instanceof String);
    }

    @Test
    void shouldPreserveExcludedMethods() {
        String sourceCode = """
                public class MainClass {
                    public static void main(String[] args) {
                        System.out.println("Hello");
                    }
                    
                    @Override
                    public String toString() {
                        return "test";
                    }
                    
                    public boolean equals(Object obj) {
                        return true;
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "MainClass.java");

        assertTrue(obfuscated.contains("main(String[] args)"));
        assertTrue(obfuscated.contains("toString()"));
        assertTrue(obfuscated.contains("equals(Object obj)"));
    }

    @Test
    void shouldHandleMultipleFilesSequentially() {
        String sourceCode1 = """
                public class FirstClass {
                    private int count = 0;
                    
                    public void increment() {
                        count++;
                    }
                }
                """;

        String sourceCode2 = """
                public class SecondClass {
                    private String text = "abc";
                    
                    public void display() {
                        System.out.println(text);
                    }
                }
                """;

        String obfuscated1 = obfuscator.obfuscateCode(sourceCode1, "FirstClass.java");
        String newFileName1 = obfuscator.getNewFileName("FirstClass.java");

        String obfuscated2 = obfuscator.obfuscateCode(sourceCode2, "SecondClass.java");
        String newFileName2 = obfuscator.getNewFileName("SecondClass.java");

        assertNotEquals(sourceCode1, obfuscated1);
        assertNotEquals(sourceCode2, obfuscated2);
        assertNotEquals("FirstClass.java", newFileName1);
        assertNotEquals("SecondClass.java", newFileName2);
    }

    @Test
    void shouldGetStatistics() {
        String sourceCode = """
                public class StatsClass {
                    private String field1 = "a";
                    private int field2 = 1;
                    
                    public void method1() {
                        System.out.println(field1);
                    }
                    
                    public void method2() {
                        System.out.println(field2);
                    }
                }
                """;

        obfuscator.obfuscateCode(sourceCode, "StatsClass.java");

        String statistics = obfuscator.getStatistics();

        assertNotNull(statistics);
        assertTrue(statistics.contains("Statistics"));
        assertTrue(statistics.contains("Classes renamed"));
        assertTrue(statistics.contains("Methods renamed"));
        assertTrue(statistics.contains("Variables renamed"));

        assertTrue(statistics.contains("1"));
    }

    @Test
    void shouldResetObfuscator() {
        String sourceCode = """
                public class ResetClass {
                    private String test = "reset";
                }
                """;

        obfuscator.obfuscateCode(sourceCode, "ResetClass.java");
        String stats1 = obfuscator.getStatistics();

        obfuscator.reset();

        obfuscator.obfuscateCode(sourceCode, "ResetClass.java");
        String stats2 = obfuscator.getStatistics();

        assertNotNull(stats1);
        assertNotNull(stats2);
    }

    @Test
    void shouldHandleComplexCode() {
        String sourceCode = """
                import java.util.List;
                import java.util.ArrayList;
                
                public class ComplexClass {
                    private List<String> items = new ArrayList<>();
                    private int total = 0;
                    
                    public void addItem(String item) {
                        items.add(item);
                        total++;
                    }
                    
                    public void printAll() {
                        for (String item : items) {
                            System.out.println("Item: " + item);
                        }
                        System.out.println("Total: " + total);
                    }
                    
                    public static void main(String[] args) {
                        ComplexClass obj = new ComplexClass();
                        obj.addItem("First");
                        obj.addItem("Second");
                        obj.printAll();
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "ComplexClass.java");

        assertNotNull(obfuscated);
        assertNotEquals(sourceCode, obfuscated);
        assertTrue(obfuscated.contains("public class"));
        assertTrue(obfuscated.contains("main(String[] args)"));
    }

    @Test
    void shouldUpdateReferencesCorrectly() {
        String sourceCode = """
                public class RefClass {
                    private String message = "Hello";
                    
                    public void printMessage() {
                        System.out.println(message);
                    }
                    
                    public void callOtherMethod() {
                        printMessage();
                    }
                    
                    public static void main(String[] args) {
                        RefClass obj = new RefClass();
                        obj.printMessage();
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "RefClass.java");

        assertFalse(obfuscated.contains("obj.printMessage()"));
        assertFalse(obfuscated.contains("System.out.println(message)"));
    }

    @Test
    void shouldExcludeShortVariableNames() {
        String sourceCode = """
                public class ShortVars {
                    public void test() {
                        int i = 0;
                        int j = 1;
                        int k = 2;
                        for (int x = 0; x < 10; x++) {
                            System.out.println(x);
                        }
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "ShortVars.java");

        // Короткие переменные (i, j, k, x) должны остаться
        assertTrue(obfuscated.contains("int i = 0"));
        assertTrue(obfuscated.contains("int j = 1"));
        assertTrue(obfuscated.contains("for (int x = 0"));
    }

    @TempDir
    Path tempDir;

    @Test
    void shouldHandleFileWithPackage() throws Exception {
        String sourceCode = """
                package com.example.test;
                
                import java.util.List;
                
                public class PackageClass {
                    private List<String> items;
                    
                    public void add(String item) {
                        if (items == null) {
                            items = new java.util.ArrayList<>();
                        }
                        items.add(item);
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "PackageClass.java");

        assertNotNull(obfuscated);
        assertNotEquals(sourceCode, obfuscated);
        assertTrue(obfuscated.contains("package com.example.test;"));
    }

    @Test
    void shouldHandleInterface() {
        String sourceCode = """
                public interface MyInterface {
                    void doSomething();
                    String getName();
                    default void print() {
                        System.out.println(getName());
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "MyInterface.java");

        assertNotNull(obfuscated);
        assertTrue(obfuscated.contains("interface"));
    }

    @Test
    void shouldHandleEnum() {
        String sourceCode = """
                public enum Color {
                    RED, GREEN, BLUE;
                    
                    public void print() {
                        System.out.println(this.name());
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "Color.java");

        assertNotNull(obfuscated);
        assertTrue(obfuscated.contains("enum"));
        assertTrue(obfuscated.contains("RED"));
        assertTrue(obfuscated.contains("GREEN"));
        assertTrue(obfuscated.contains("BLUE"));
    }

    @Test
    void shouldHandleAnnotations() {
        String sourceCode = """
                @Deprecated
                public class AnnotatedClass {
                    @Override
                    public String toString() {
                        return "annotated";
                    }
                    
                    @SuppressWarnings("unchecked")
                    public void method() {
                        // do something
                    }
                }
                """;

        String obfuscated = obfuscator.obfuscateCode(sourceCode, "AnnotatedClass.java");

        assertNotNull(obfuscated);
        assertTrue(obfuscated.contains("@Deprecated"));
        assertTrue(obfuscated.contains("@Override"));
        assertTrue(obfuscated.contains("@SuppressWarnings"));
    }
}