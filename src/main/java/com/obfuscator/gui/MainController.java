package com.obfuscator.gui;

import com.obfuscator.ObfuscationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ObfuscationService obfuscationService;
    private Stage primaryStage;

    @FXML
    private TextField inputField;
    @FXML
    private TextField outputField;
    @FXML
    private TextArea logArea;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;
    @FXML
    private Button startButton;
    @FXML
    private Button browseInputButton;
    @FXML
    private Button browseOutputButton;

    @FXML
    private void initialize() {
        progressBar.setVisible(false);
        statusLabel.setText("Ready");
        startButton.setDisable(true);
    }

    public void setObfuscationService(ObfuscationService service) {
        this.obfuscationService = service;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void exportLogs() {

        try {

            log("Exporting logs to file...");
            // TODO: реализовать сохранение логов
            showAlert(Alert.AlertType.INFORMATION, "Export",
                    "Log export feature will be implemented soon");
        } catch (Exception e) {
            log("Error exporting logs: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Export Error",
                    "Failed to export logs: " + e.getMessage());
        }
    }

    @FXML
    private void browseInput() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Input Directory");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDir = chooser.showDialog(primaryStage);
        if (selectedDir != null) {
            inputField.setText(selectedDir.getAbsolutePath());
            log("Selected input directory: " + selectedDir.getAbsolutePath());
            validateInputs();
        }
    }

    @FXML
    private void browseOutput() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Output Directory");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDir = chooser.showDialog(primaryStage);
        if (selectedDir != null) {
            outputField.setText(selectedDir.getAbsolutePath());
            log("Selected output directory: " + selectedDir.getAbsolutePath());
            validateInputs();
        }
    }

    @FXML
    private void startObfuscation() {
        if (inputField.getText().isEmpty() || outputField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select input and output directories");
            return;
        }

        Path inputPath = Path.of(inputField.getText());
        Path outputPath = Path.of(outputField.getText());

        if (!inputPath.toFile().exists()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Input directory does not exist");
            return;
        }

        if (!inputPath.toFile().isDirectory()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Input must be a directory");
            return;
        }

        ObfuscationTask task = new ObfuscationTask(inputPath, outputPath, obfuscationService);

        log("Starting obfuscation...");
        log("Input: " + inputPath);
        log("Output: " + outputPath);

        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setVisible(true);
        statusLabel.textProperty().bind(task.messageProperty());

        startButton.setDisable(true);
        browseInputButton.setDisable(true);
        browseOutputButton.setDisable(true);

        task.setOnSucceeded(event -> {
            var result = task.getValue();
            progressBar.setVisible(false);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Completed");

            enableButtons();

            log("Obfuscation completed!");
            log("Processed " + result.size() + " files");

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Successfully obfuscated " + result.size() + " files");
        });

        task.setOnFailed(event -> {
            progressBar.setVisible(false);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Failed");

            enableButtons();

            Throwable ex = task.getException();
            log("Error: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearLogs() {
        logArea.clear();
    }

    private void enableButtons() {
        startButton.setDisable(false);
        browseInputButton.setDisable(false);
        browseOutputButton.setDisable(false);
    }

    private void validateInputs() {
        boolean isValid = !inputField.getText().isEmpty() && !outputField.getText().isEmpty();
        startButton.setDisable(!isValid);
    }

    private void log(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            logArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Java Code Obfuscator");
        alert.setHeaderText("Java Code Obfuscator v1.0.0");
        alert.setContentText(
                "A tool for obfuscating Java source code.\n\n" +
                        "Features:\n" +
                        "• Renames classes, methods, and variables\n" +
                        "• Preserves code functionality\n" +
                        "• Supports directory obfuscation\n" +
                        "• CLI and GUI modes\n\n" +
                        "Created for educational purposes."
        );
        alert.showAndWait();
    }
}