package com.obfuscator.fileprocessor;

import com.obfuscator.obfuscator.CodeObfuscator;
import com.obfuscator.obfuscator.ObfuscationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FileProcessor {

    private static final Logger logger = LogManager.getLogger(FileProcessor.class);

    private final CodeObfuscator obfuscator;

    private final AtomicInteger filesProcessed = new AtomicInteger(0);
    private final AtomicInteger filesSkipped = new AtomicInteger(0);
    private final AtomicInteger filesFailed = new AtomicInteger(0);

    public FileProcessor(CodeObfuscator obfuscator) {
        this.obfuscator = obfuscator;
        logger.debug("FileProcessor initialized");
    }

    public List<Path> processDirectory(Path inputDir, Path outputDir) throws IOException {
        try {
            logger.info("Processing directory: {}", inputDir);

            List<Path> javaFiles = findJavaFiles(inputDir);
            logger.info("Found {} Java files", javaFiles.size());

            if (javaFiles.isEmpty()) {
                logger.warn("No Java files found in directory: {}", inputDir);
                return new ArrayList<>();
            }

            Files.createDirectories(outputDir);
            logger.info("Output directory created/verified: {}", outputDir);

            List<Path> processedFiles = new ArrayList<>();

            for (Path javaFile : javaFiles) {
                try {
                    logger.debug("Processing file: {}", javaFile);
                    Path processedFile = processSingleFile(javaFile, inputDir, outputDir);
                    processedFiles.add(processedFile);
                    filesProcessed.incrementAndGet();
                    logger.info("✅ Processed file: {} -> {}", javaFile.getFileName(), processedFile.getFileName());
                } catch (ObfuscationException e) {
                    filesFailed.incrementAndGet();
                    logger.error("❌ Failed to process file {}: {}", javaFile, e.getMessage());
                } catch (Exception e) {
                    filesSkipped.incrementAndGet();
                    logger.warn("⚠️ Skipped file {} due to unexpected error: {}", javaFile, e.getMessage());
                }
            }

            logger.info("Directory processing completed. " +
                            "✅ Processed: {}, ❌ Failed: {}, ⚠️ Skipped: {}",
                    filesProcessed.get(), filesFailed.get(), filesSkipped.get());

            return processedFiles;

        } catch (IOException e) {
            logger.error("IO error processing directory: {}", e.getMessage(), e);
            throw new ObfuscationException("IO error: " + e.getMessage(), e);
        }
    }

    public Path processSingleFile(Path javaFile, Path inputDir, Path outputDir)
            throws IOException, ObfuscationException {

        logger.debug("Processing single file: {}", javaFile);

        String content;
        try {
            // Читаем файл старым способом (работает в Java 8+)
            content = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ObfuscationException("Failed to read file: " + javaFile, e);
        }

        String fileName = javaFile.getFileName().toString();

        String obfuscatedContent = obfuscator.obfuscateCode(content, fileName);

        // Проверяем, изменился ли код
        if (content.equals(obfuscatedContent)) {
            logger.warn("File {} was not obfuscated (identical to original)", fileName);
        } else {
            logger.debug("File {} successfully obfuscated", fileName);
        }

        String newFileName = obfuscator.getNewFileName(fileName);

        Path relativePath = inputDir.relativize(javaFile.getParent());
        Path outputPath = outputDir.resolve(relativePath).resolve(newFileName);

        try {
            Files.createDirectories(outputPath.getParent());
        } catch (IOException e) {
            throw new ObfuscationException("Failed to create directories: " + outputPath.getParent(), e);
        }

        try {
            // Пишем файл старым способом
            Files.write(outputPath, obfuscatedContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.debug("File saved: {}", outputPath);
        } catch (IOException e) {
            throw new ObfuscationException("Failed to write file: " + outputPath, e);
        }

        return outputPath;
    }

    private List<Path> findJavaFiles(Path directory) throws IOException {
        List<Path> javaFiles = new ArrayList<>();

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().toLowerCase().endsWith(".java")) {
                    javaFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                logger.warn("Cannot access file: {} - {}", file, exc.getMessage());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName().toString();
                if (dirName.startsWith(".") ||
                        dirName.equals("node_modules") ||
                        dirName.equals("target") ||
                        dirName.equals("build")) {
                    logger.debug("Skipping directory: {}", dirName);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return javaFiles;
    }

    public String getStatistics() {
        return String.format(
                "File Processing Statistics:%n" +
                        "  ✅ Files processed: %d%n" +
                        "  ❌ Files failed: %d%n" +
                        "  ⚠️ Files skipped: %d",
                filesProcessed.get(),
                filesFailed.get(),
                filesSkipped.get()
        );
    }

    public void resetStatistics() {
        filesProcessed.set(0);
        filesFailed.set(0);
        filesSkipped.set(0);
        logger.debug("FileProcessor statistics reset");
    }
}