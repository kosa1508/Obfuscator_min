package com.obfuscator.fileprocessor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileWalker {

    public static List<Path> findJavaFiles(Path startDir) throws IOException {
        List<Path> javaFiles = new ArrayList<>();
        JavaFileVisitor visitor = new JavaFileVisitor(javaFiles);
        Files.walkFileTree(startDir, visitor);
        return javaFiles;
    }

    private static class JavaFileVisitor extends SimpleFileVisitor<Path> {
        private final List<Path> javaFiles;

        public JavaFileVisitor(List<Path> javaFiles) {
            this.javaFiles = javaFiles;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (isJavaFile(file)) {
                javaFiles.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            String dirName = dir.getFileName().toString();
            if (dirName.startsWith(".") ||
                    dirName.equals("target") ||
                    dirName.equals("build") ||
                    dirName.equals("node_modules") ||
                    dirName.equals(".git") ||
                    dirName.equals(".idea")) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        private boolean isJavaFile(Path file) {
            String fileName = file.getFileName().toString();
            return fileName.toLowerCase().endsWith(".java");
        }
    }
}
