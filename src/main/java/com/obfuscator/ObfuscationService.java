package com.obfuscator;

import com.obfuscator.fileprocessor.FileProcessor;
import com.obfuscator.obfuscator.CodeObfuscator;
import com.obfuscator.obfuscator.ObfuscationException;
import com.obfuscator.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class ObfuscationService {

    private static final Logger logger = LogManager.getLogger(ObfuscationService.class);

    private final CodeObfuscator codeObfuscator;
    private final FileProcessor fileProcessor;
    private final ValidationUtil validationUtil;

    public ObfuscationService() {
        this.codeObfuscator = new CodeObfuscator();
        this.fileProcessor = new FileProcessor(codeObfuscator);
        this.validationUtil = new ValidationUtil();
        logger.debug("ObfuscationService initialized");
    }

    public List<Path> processDirectory(Path inputDir, Path outputDir) throws ObfuscationException {
        try {
            logger.info("Starting directory processing: {} -> {}", inputDir, outputDir);

            validationUtil.validateInputDirectory(inputDir);
            validationUtil.validateOutputDirectory(outputDir);

            List<Path> processedFiles = fileProcessor.processDirectory(inputDir, outputDir);

            logger.info("Directory processing completed. {} files processed.", processedFiles.size());
            return processedFiles;

        } catch (Exception e) {
            logger.error("Error processing directory: {}", e.getMessage(), e);
            throw new ObfuscationException("Failed to process directory: " + e.getMessage(), e);
        }
    }

    public Path processSingleFile(Path inputFile, Path outputDir) throws ObfuscationException {
        try {
            logger.info("Processing single file: {} -> {}", inputFile, outputDir);

            validationUtil.validateJavaFile(inputFile);
            validationUtil.validateOutputDirectory(outputDir);

            Path processedFile = fileProcessor.processSingleFile(inputFile, inputFile.getParent(), outputDir);

            logger.info("File processing completed: {}", processedFile);
            return processedFile;

        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            throw new ObfuscationException("Failed to process file: " + e.getMessage(), e);
        }
    }

    public String getStatistics() {
        return codeObfuscator.getStatistics();
    }

    public void reset() {
        codeObfuscator.reset();
        logger.debug("ObfuscationService reset");
    }
}
