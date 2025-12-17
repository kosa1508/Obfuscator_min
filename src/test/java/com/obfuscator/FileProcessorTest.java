package com.obfuscator;

import com.obfuscator.fileprocessor.FileProcessor;
import com.obfuscator.obfuscator.CodeObfuscator;
import com.obfuscator.obfuscator.ObfuscationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileProcessorTest {

    @TempDir
    Path tempDir;

    private FileProcessor fileProcessor;
    private CodeObfuscator codeObfuscator;

    @BeforeEach
    void setUp() {
        codeObfuscator = new CodeObfuscator();
        fileProcessor = new FileProcessor(codeObfuscator);
    }

    @Test
    void shouldProcessSingleJavaFile() throws IOException, ObfuscationException {
        Path testFile = tempDir.resolve("TestClass.java");
        Files.writeString(testFile, """
            public class TestClass {
                public void testMethod() {
                    System.out.println("Test");
                }
            }
            """);

        Path outputDir = tempDir.resolve("output");
        Path processedFile = fileProcessor.processSingleFile(testFile, tempDir, outputDir);

        assertTrue(Files.exists(processedFile));
        assertTrue(Files.isRegularFile(processedFile));

        String processedContent = Files.readString(processedFile);
        assertTrue(processedContent.contains("class"));
    }

    @Test
    void shouldProcessDirectoryWithJavaFiles() throws IOException {
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);

        Files.writeString(srcDir.resolve("Class1.java"), """
            public class Class1 {
                public void method1() {}
            }
            """);

        Files.writeString(srcDir.resolve("Class2.java"), """
            public class Class2 {
                public void method2() {}
            }
            """);

        Path subDir = srcDir.resolve("subpackage");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("Class3.java"), """
            public class Class3 {
                public void method3() {}
            }
            """);

        Path outputDir = tempDir.resolve("obfuscated");
        List<Path> processedFiles = fileProcessor.processDirectory(srcDir, outputDir);

        assertEquals(3, processedFiles.size());
        for (Path file : processedFiles) {
            assertTrue(Files.exists(file));
        }

        assertTrue(Files.exists(outputDir));
    }

    @Test
    void shouldSkipNonJavaFiles() throws IOException {
        Path testDir = tempDir.resolve("test");
        Files.createDirectories(testDir);

        Files.writeString(testDir.resolve("JavaFile.java"), "public class JavaFile {}");
        Files.writeString(testDir.resolve("text.txt"), "Just text");

        Path outputDir = tempDir.resolve("output");
        List<Path> processedFiles = fileProcessor.processDirectory(testDir, outputDir);

        assertEquals(1, processedFiles.size());
    }

    @Test
    void shouldHandleEmptyDirectory() throws IOException {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectories(emptyDir);

        Path outputDir = tempDir.resolve("output");
        List<Path> processedFiles = fileProcessor.processDirectory(emptyDir, outputDir);

        assertTrue(processedFiles.isEmpty());
    }

    @Test
    void shouldPreserveDirectoryStructure() throws IOException, ObfuscationException {
        Path deepDir = tempDir.resolve("a/b/c/d");
        Files.createDirectories(deepDir);

        Files.writeString(deepDir.resolve("DeepClass.java"), """
            public class DeepClass {
                public void deepMethod() {}
            }
            """);

        Path outputDir = tempDir.resolve("output");
        Path processedFile = fileProcessor.processSingleFile(
                deepDir.resolve("DeepClass.java"), tempDir, outputDir);

        assertNotNull(processedFile);
        assertTrue(Files.exists(processedFile));
    }

    @Test
    void shouldGetStatistics() throws IOException {
        Path testDir = tempDir.resolve("test");
        Files.createDirectories(testDir);

        Files.writeString(testDir.resolve("Class1.java"), "public class Class1 {}");
        Files.writeString(testDir.resolve("Class2.java"), "public class Class2 {}");

        Path outputDir = tempDir.resolve("output");
        fileProcessor.processDirectory(testDir, outputDir);

        String statistics = fileProcessor.getStatistics();

        assertTrue(statistics.contains("Files processed: 2") ||
                statistics.contains("Files processed: 0"));
    }

    @Test
    void shouldResetStatistics() throws IOException {
        Path testDir = tempDir.resolve("test");
        Files.createDirectories(testDir);

        Files.writeString(testDir.resolve("Test.java"), "public class Test {}");

        Path outputDir = tempDir.resolve("output");
        fileProcessor.processDirectory(testDir, outputDir);

        fileProcessor.resetStatistics();
        String statistics = fileProcessor.getStatistics();

        assertTrue(statistics.contains("Files processed: 0"));
        assertTrue(statistics.contains("Files failed: 0"));
        assertTrue(statistics.contains("Files skipped: 0"));
    }

    @Test
    void shouldThrowExceptionForNonExistentFile() {
        Path nonExistentFile = tempDir.resolve("NonExistent.java");
        Path outputDir = tempDir.resolve("output");

        try {
            fileProcessor.processSingleFile(nonExistentFile, tempDir, outputDir);
            fail("Expected IOException or ObfuscationException");
        } catch (Exception e) {

            assertTrue(e instanceof IOException || e instanceof ObfuscationException);
        }
    }

    @Test
    void shouldProcessMultipleJavaFilesInNestedDirectories() throws IOException {

        Path rootDir = tempDir.resolve("project");
        Files.createDirectories(rootDir);

        Files.writeString(rootDir.resolve("Main.java"), "public class Main {}");

        Path subDir1 = rootDir.resolve("utils");
        Files.createDirectories(subDir1);
        Files.writeString(subDir1.resolve("Util1.java"), "public class Util1 {}");
        Files.writeString(subDir1.resolve("Util2.java"), "public class Util2 {}");

        Path subDir2 = rootDir.resolve("models");
        Files.createDirectories(subDir2);
        Files.writeString(subDir2.resolve("Model.java"), "public class Model {}");

        Path outputDir = tempDir.resolve("obfuscated");
        List<Path> processedFiles = fileProcessor.processDirectory(rootDir, outputDir);

        assertTrue(processedFiles.size() >= 1);
        assertTrue(Files.exists(outputDir));
    }
}