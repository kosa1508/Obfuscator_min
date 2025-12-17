package com.obfuscator.util;

import com.obfuscator.obfuscator.ObfuscationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ValidationUtil {

    public void validateInputDirectory(Path directory) throws ObfuscationException {
        if (directory == null) {
            throw new ObfuscationException("Input directory cannot be null");
        }

        if (!Files.exists(directory)) {
            throw new ObfuscationException("Input directory does not exist: " + directory);
        }

        if (!Files.isDirectory(directory)) {
            throw new ObfuscationException("Input path is not a directory: " + directory);
        }

        if (!Files.isReadable(directory)) {
            throw new ObfuscationException("No read permission for directory: " + directory);
        }

        LoggerUtil.getLogger(ValidationUtil.class).debug("Input directory validated: {}", directory);
    }

    public void validateOutputDirectory(Path directory) throws ObfuscationException {
        if (directory == null) {
            throw new ObfuscationException("Output directory cannot be null");
        }

        if (Files.exists(directory)) {
            if (!Files.isDirectory(directory)) {
                throw new ObfuscationException("Output path exists but is not a directory: " + directory);
            }

            if (!Files.isWritable(directory)) {
                throw new ObfuscationException("No write permission for directory: " + directory);
            }
        } else {
            Path parent = directory.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    throw new ObfuscationException("Cannot create parent directory: " + parent, e);
                }
            }
        }

        LoggerUtil.getLogger(ValidationUtil.class).debug("Output directory validated: {}", directory);
    }

    public void validateJavaFile(Path file) throws ObfuscationException {
        if (file == null) {
            throw new ObfuscationException("File cannot be null");
        }

        if (!Files.exists(file)) {
            throw new ObfuscationException("File does not exist: " + file);
        }

        if (!Files.isRegularFile(file)) {
            throw new ObfuscationException("Path is not a regular file: " + file);
        }

        if (!Files.isReadable(file)) {
            throw new ObfuscationException("No read permission for file: " + file);
        }

        String fileName = file.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".java")) {
            throw new ObfuscationException("File is not a Java file: " + file);
        }

        LoggerUtil.getLogger(ValidationUtil.class).debug("Java file validated: {}", file);
    }

    public void validateCanCreateFile(Path file) throws ObfuscationException {
        if (file == null) {
            throw new ObfuscationException("File cannot be null");
        }

        if (Files.exists(file)) {
            if (!Files.isWritable(file)) {
                throw new ObfuscationException("File exists but is not writable: " + file);
            }
        } else {
            Path parent = file.getParent();
            if (parent != null) {
                validateOutputDirectory(parent);
            }
        }

        LoggerUtil.getLogger(ValidationUtil.class).debug("File creation validated: {}", file);
    }

    public void validateString(String str, String fieldName) throws ObfuscationException {
        if (str == null) {
            throw new ObfuscationException(fieldName + " cannot be null");
        }

        if (str.trim().isEmpty()) {
            throw new ObfuscationException(fieldName + " cannot be empty");
        }
    }

    public boolean isValidJavaIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        char firstChar = identifier.charAt(0);
        if (!Character.isJavaIdentifierStart(firstChar)) {
            return false;
        }

        for (int i = 1; i < identifier.length(); i++) {
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                return false;
            }
        }

        return !isJavaKeyword(identifier);
    }

    private boolean isJavaKeyword(String word) {
        String[] keywords = {
                "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                "char", "class", "const", "continue", "default", "do", "double",
                "else", "enum", "extends", "final", "finally", "float", "for",
                "goto", "if", "implements", "import", "instanceof", "int",
                "interface", "long", "native", "new", "package", "private",
                "protected", "public", "return", "short", "static", "strictfp",
                "super", "switch", "synchronized", "this", "throw", "throws",
                "transient", "try", "void", "volatile", "while"
        };

        for (String keyword : keywords) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }
}
