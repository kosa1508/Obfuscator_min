package com.obfuscator.gui;

import com.obfuscator.ObfuscationService;
import com.obfuscator.obfuscator.ObfuscationException;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class ObfuscationTask extends Task<List<Path>> {

    private static final Logger logger = LogManager.getLogger(ObfuscationTask.class);

    private final Path inputPath;
    private final Path outputPath;
    private final ObfuscationService obfuscationService;
    private final boolean isSingleFile;

    public ObfuscationTask(Path inputPath, Path outputPath, ObfuscationService obfuscationService) {
        this(inputPath, outputPath, obfuscationService, false);
    }

    public ObfuscationTask(Path inputPath, Path outputPath, ObfuscationService obfuscationService, boolean isSingleFile) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.obfuscationService = obfuscationService;
        this.isSingleFile = isSingleFile;
    }

    @Override
    protected List<Path> call() throws Exception {
        try {
            if (isSingleFile) {
                updateMessage("Processing single file...");
                logger.info("Starting single file obfuscation: {} -> {}", inputPath, outputPath);

                updateMessage("Validating file...");
                Path result = obfuscationService.processSingleFile(inputPath, outputPath);

                updateMessage("File processed successfully");
                updateProgress(1, 1);

                logger.info("Single file obfuscation completed: {}", result);
                return List.of(result);

            } else {
                updateMessage("Searching for Java files...");
                logger.info("Starting folder obfuscation: {} -> {}", inputPath, outputPath);

                updateMessage("Processing files...");
                List<Path> result = obfuscationService.processDirectory(inputPath, outputPath);

                updateMessage("Generating report...");
                updateProgress(1, 1);

                logger.info("Folder obfuscation completed. Processed {} files.", result.size());
                return result;
            }

        } catch (ObfuscationException e) {
            logger.error("Obfuscation error in task", e);
            updateMessage("Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in task", e);
            updateMessage("Unexpected error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void scheduled() {
        logger.debug("Obfuscation task scheduled");
        updateMessage("Task scheduled...");
    }

    @Override
    protected void running() {
        logger.debug("Obfuscation task running");
        updateMessage("Running...");
    }

    @Override
    protected void succeeded() {
        logger.debug("Obfuscation task succeeded");
        updateMessage("Task completed successfully");
    }

    @Override
    protected void cancelled() {
        logger.debug("Obfuscation task cancelled");
        updateMessage("Task cancelled");
    }

    @Override
    protected void failed() {
        logger.debug("Obfuscation task failed");
        updateMessage("Task failed");
    }
}