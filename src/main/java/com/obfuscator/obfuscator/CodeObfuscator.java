package com.obfuscator.obfuscator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CodeObfuscator {

    private static final Logger logger = LogManager.getLogger(CodeObfuscator.class);

    private final JavaParser javaParser;
    private final NameGenerator nameGenerator;
    private final Map<String, String> symbolTable;
    private final Map<String, String> classRenames;

    private final AtomicInteger classesRenamed = new AtomicInteger(0);
    private final AtomicInteger methodsRenamed = new AtomicInteger(0);
    private final AtomicInteger variablesRenamed = new AtomicInteger(0);

    private static final Set<String> EXCLUDED_CLASSES = Set.of(
            "String", "Integer", "Long", "Double", "Float", "Boolean",
            "List", "Map", "Set", "ArrayList", "HashMap", "HashSet"
    );

    private static final Set<String> EXCLUDED_METHODS = Set.of(
            "main", "toString", "equals", "hashCode", "clone",
            "compareTo", "wait", "notify", "notifyAll"
    );

    private static final Set<String> EXCLUDED_VARIABLES = Set.of(
            "args", "this", "super", "out", "err", "in",
            "class", "length", "size"
    );

    public CodeObfuscator() {
        this.javaParser = new JavaParser();
        this.nameGenerator = new NameGenerator();
        this.symbolTable = new HashMap<>();
        this.classRenames = new HashMap<>();
        logger.debug("CodeObfuscator initialized");
    }

    public String obfuscateCode(String sourceCode, String originalFileName) throws ObfuscationException {
        try {
            logger.debug("Obfuscating file: {}", originalFileName);

            ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);

            if (!parseResult.isSuccessful() || !parseResult.getResult().isPresent()) {
                logger.error("Failed to parse Java code from file: {}", originalFileName);
                throw new ObfuscationException("Failed to parse Java code");
            }

            CompilationUnit compilationUnit = parseResult.getResult().get();

            resetFileStatistics();

            logger.debug("Applying obfuscation visitors...");

            String originalClassName = getClassNameFromFileName(originalFileName);

            ClassObfuscatorVisitor classVisitor = new ClassObfuscatorVisitor(originalClassName);
            classVisitor.visit(compilationUnit, null);

            MethodObfuscatorVisitor methodVisitor = new MethodObfuscatorVisitor();
            methodVisitor.visit(compilationUnit, null);

            VariableObfuscatorVisitor variableVisitor = new VariableObfuscatorVisitor();
            variableVisitor.visit(compilationUnit, null);

            updateReferences(compilationUnit);

            updateImports(compilationUnit);

            String obfuscatedCode = compilationUnit.toString();

            logger.debug("Obfuscation complete for: {}", originalFileName);
            logger.debug("Statistics: classes={}, methods={}, variables={}",
                    classesRenamed.get(), methodsRenamed.get(), variablesRenamed.get());

            return obfuscatedCode;

        } catch (Exception e) {
            logger.error("Error during obfuscation of {}: {}", originalFileName, e.getMessage(), e);
            throw new ObfuscationException("Obfuscation failed: " + e.getMessage(), e);
        }
    }

    private String getClassNameFromFileName(String fileName) {
        return fileName.replace(".java", "");
    }

    public String getNewFileName(String originalFileName) {
        String className = getClassNameFromFileName(originalFileName);
        String newClassName = classRenames.get(className);

        if (newClassName != null) {
            logger.debug("File rename: {} -> {}", originalFileName, newClassName + ".java");
            return newClassName + ".java";
        }

        logger.debug("Class not renamed, keeping original filename: {}", originalFileName);
        return originalFileName;
    }

    private class ClassObfuscatorVisitor extends ModifierVisitor<Void> {
        private final String fileNameClassName;

        public ClassObfuscatorVisitor(String fileNameClassName) {
            this.fileNameClassName = fileNameClassName;
        }

        @Override
        public Visitable visit(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration n, Void arg) {
            String originalName = n.getNameAsString();

            logger.trace("Processing class: {}", originalName);

            boolean shouldRename = false;
            String classNameToCheck = originalName;

            if (originalName.equals(fileNameClassName)) {
                shouldRename = true;
            }

            if (!originalName.isEmpty() && !isExcludedClass(originalName) && shouldRename) {
                String newName = nameGenerator.generateClassName();
                classRenames.put(originalName, newName);
                n.setName(newName);
                classesRenamed.incrementAndGet();
                logger.debug("Renamed class: {} -> {}", originalName, newName);
            } else {
                if (isExcludedClass(originalName)) {
                    logger.trace("Class '{}' is excluded", originalName);
                } else if (!shouldRename) {
                    logger.trace("Class '{}' doesn't match filename '{}'", originalName, fileNameClassName);
                }
            }
            return super.visit(n, arg);
        }

        private boolean isExcludedClass(String className) {
            boolean excluded = EXCLUDED_CLASSES.contains(className) ||
                    className.length() <= 2 ||
                    className.startsWith("Test") ||
                    className.endsWith("Test");

            return excluded;
        }
    }

    private class MethodObfuscatorVisitor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(com.github.javaparser.ast.body.MethodDeclaration n, Void arg) {
            String methodName = n.getNameAsString();

            logger.trace("Processing method: {}", methodName);

            if (!methodName.isEmpty() && !isExcludedMethod(methodName)) {
                String newName = nameGenerator.generateMethodName();
                symbolTable.put(methodName, newName);
                n.setName(newName);
                methodsRenamed.incrementAndGet();
                logger.debug("Renamed method: {} -> {}", methodName, newName);
            } else {
                logger.trace("Method excluded from renaming: {}", methodName);
            }
            return super.visit(n, arg);
        }

        private boolean isExcludedMethod(String methodName) {
            boolean excluded = EXCLUDED_METHODS.contains(methodName) ||
                    methodName.length() <= 2 ||
                    methodName.startsWith("get") ||
                    methodName.startsWith("set") ||
                    methodName.startsWith("is");

            return excluded;
        }
    }

    private class VariableObfuscatorVisitor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(com.github.javaparser.ast.body.VariableDeclarator n, Void arg) {
            String varName = n.getNameAsString();

            logger.trace("Processing variable: {}", varName);

            if (!varName.isEmpty() && !isExcludedVariable(varName)) {
                String newName = nameGenerator.generateVariableName();
                symbolTable.put(varName, newName);
                n.setName(newName);
                variablesRenamed.incrementAndGet();
                logger.debug("Renamed variable: {} -> {}", varName, newName);
            } else {
                logger.trace("Variable excluded from renaming: {}", varName);
            }
            return super.visit(n, arg);
        }

        private boolean isExcludedVariable(String varName) {
            boolean excluded = EXCLUDED_VARIABLES.contains(varName) ||
                    varName.length() <= 1 ||
                    varName.matches("[ijklm]");

            return excluded;
        }
    }

    private void updateReferences(CompilationUnit compilationUnit) {
        symbolTable.forEach((oldName, newName) -> {
            compilationUnit.accept(new ModifierVisitor<Void>() {
                @Override
                public Visitable visit(com.github.javaparser.ast.expr.NameExpr n, Void arg) {
                    if (n.getNameAsString().equals(oldName)) {
                        n.setName(newName);
                        logger.trace("Updated variable reference: {} -> {}", oldName, newName);
                    }
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(com.github.javaparser.ast.expr.FieldAccessExpr n, Void arg) {
                    if (n.getNameAsString().equals(oldName)) {
                        n.setName(newName);
                        logger.trace("Updated field access: {} -> {}", oldName, newName);
                    }
                    return super.visit(n, arg);
                }
            }, null);
        });

        symbolTable.forEach((oldName, newName) -> {
            compilationUnit.accept(new ModifierVisitor<Void>() {
                @Override
                public Visitable visit(com.github.javaparser.ast.expr.MethodCallExpr n, Void arg) {
                    if (n.getNameAsString().equals(oldName)) {
                        n.setName(newName);
                        logger.trace("Updated method call: {} -> {}", oldName, newName);
                    }
                    return super.visit(n, arg);
                }
            }, null);
        });

        classRenames.forEach((oldClass, newClass) -> {
            compilationUnit.accept(new ModifierVisitor<Void>() {
                @Override
                public Visitable visit(com.github.javaparser.ast.expr.ObjectCreationExpr n, Void arg) {
                    if (n.getType().toString().equals(oldClass)) {
                        n.setType(newClass);
                        logger.trace("Updated object creation: {} -> {}", oldClass, newClass);
                    }
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(com.github.javaparser.ast.type.ClassOrInterfaceType n, Void arg) {
                    if (n.getNameAsString().equals(oldClass)) {
                        n.setName(newClass);
                        logger.trace("Updated type reference: {} -> {}", oldClass, newClass);
                    }
                    return super.visit(n, arg);
                }
            }, null);
        });
    }

    private void updateImports(CompilationUnit compilationUnit) {
        compilationUnit.getImports().forEach(importDecl -> {
            String importName = importDecl.getNameAsString();
            classRenames.forEach((oldName, newName) -> {
                if (importName.endsWith("." + oldName)) {
                    String newImport = importName.substring(0, importName.length() - oldName.length()) + newName;
                    importDecl.setName(newImport);
                    logger.debug("Updated import: {} -> {}", importName, newImport);
                }
            });
        });
    }

    private void resetFileStatistics() {
        symbolTable.clear();
    }

    public String getStatistics() {
        return String.format(
                "Obfuscation Statistics:%n  Classes renamed: %d%n  Methods renamed: %d%n  Variables renamed: %d%n  Total symbols: %d",
                classesRenamed.get(),
                methodsRenamed.get(),
                variablesRenamed.get(),
                symbolTable.size() + classRenames.size()
        );
    }

    public void reset() {
        nameGenerator.reset();
        symbolTable.clear();
        classRenames.clear();
        classesRenamed.set(0);
        methodsRenamed.set(0);
        variablesRenamed.set(0);
        logger.debug("CodeObfuscator reset");
    }
}