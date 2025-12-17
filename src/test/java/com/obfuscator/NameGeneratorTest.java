package com.obfuscator;

import com.obfuscator.obfuscator.NameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

class NameGeneratorTest {

    private NameGenerator nameGenerator;

    @BeforeEach
    void setUp() {
        nameGenerator = new NameGenerator();
    }

    @Test
    void shouldGenerateClassName() {
        String className = nameGenerator.generateClassName();

        assertNotNull(className);
        assertFalse(className.isBlank());
        assertTrue(className.startsWith("C"));
        assertTrue(className.length() > 4);
    }

    @Test
    void shouldGenerateMethodName() {
        String methodName = nameGenerator.generateMethodName();

        assertNotNull(methodName);
        assertFalse(methodName.isBlank());
        assertTrue(methodName.startsWith("m"));
        assertTrue(methodName.length() > 4);
    }

    @Test
    void shouldGenerateVariableName() {
        String variableName = nameGenerator.generateVariableName();

        assertNotNull(variableName);
        assertFalse(variableName.isBlank());
        assertTrue(variableName.startsWith("v"));
        assertTrue(variableName.length() > 4);
    }

    @RepeatedTest(100)
    void shouldGenerateUniqueNames() {
        String name1 = nameGenerator.generateClassName();
        String name2 = nameGenerator.generateMethodName();
        String name3 = nameGenerator.generateVariableName();

        assertNotEquals(name1, name2);
        assertNotEquals(name1, name3);
        assertNotEquals(name2, name3);
    }

    @Test
    void shouldCheckIfNameIsUsed() {
        String className = nameGenerator.generateClassName();

        assertTrue(nameGenerator.isNameUsed(className));
        assertFalse(nameGenerator.isNameUsed("NonExistentName"));
    }

    @Test
    void shouldGetGeneratedNamesCount() {
        int initialCount = nameGenerator.getGeneratedNamesCount();

        nameGenerator.generateClassName();
        nameGenerator.generateMethodName();
        nameGenerator.generateVariableName();

        int newCount = nameGenerator.getGeneratedNamesCount();
        assertEquals(initialCount + 3, newCount);
    }

    @Test
    void shouldResetGenerator() {
        nameGenerator.generateClassName();
        nameGenerator.generateMethodName();

        int countBeforeReset = nameGenerator.getGeneratedNamesCount();
        nameGenerator.reset();
        int countAfterReset = nameGenerator.getGeneratedNamesCount();

        assertTrue(countBeforeReset > 0);
        assertEquals(0, countAfterReset);
    }

    @Test
    void shouldGenerateNamesWithDifferentPrefixes() {
        String className = nameGenerator.generateClassName();
        String methodName = nameGenerator.generateMethodName();
        String variableName = nameGenerator.generateVariableName();

        assertTrue(className.startsWith("C"));
        assertTrue(methodName.startsWith("m"));
        assertTrue(variableName.startsWith("v"));
    }
}