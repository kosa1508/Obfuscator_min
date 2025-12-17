package com.obfuscator.obfuscator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class NameGenerator {

    private static final Logger logger = LogManager.getLogger(NameGenerator.class);

    private final Set<String> usedNames;
    private final Random random;

    private static final String CLASS_PREFIX = "C";
    private static final String METHOD_PREFIX = "m";
    private static final String VARIABLE_PREFIX = "v";

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + DIGITS;

    private static final int MIN_SUFFIX_LENGTH = 4;
    private static final int MAX_SUFFIX_LENGTH = 8;

    public NameGenerator() {
        this.usedNames = new HashSet<>();
        this.random = new Random();
        logger.debug("NameGenerator initialized");
    }

    public String generateClassName() {
        return generateUniqueName(CLASS_PREFIX);
    }

    public String generateMethodName() {
        return generateUniqueName(METHOD_PREFIX);
    }

    public String generateVariableName() {
        return generateUniqueName(VARIABLE_PREFIX);
    }

    private String generateUniqueName(String prefix) {
        String name;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            if (attempts++ > MAX_ATTEMPTS) {
                throw new ObfuscationException("Failed to generate unique name after " + MAX_ATTEMPTS + " attempts");
            }
            name = prefix + generateRandomSuffix();
        } while (usedNames.contains(name));

        usedNames.add(name);
        logger.trace("Generated name: {}", name);
        return name;
    }

    private String generateRandomSuffix() {
        int length = MIN_SUFFIX_LENGTH + random.nextInt(MAX_SUFFIX_LENGTH - MIN_SUFFIX_LENGTH + 1);
        StringBuilder suffix = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            suffix.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        return suffix.toString();
    }

    public boolean isNameUsed(String name) {
        return usedNames.contains(name);
    }

    public int getGeneratedNamesCount() {
        return usedNames.size();
    }

    public void reset() {
        usedNames.clear();
        logger.debug("NameGenerator reset");
    }
}