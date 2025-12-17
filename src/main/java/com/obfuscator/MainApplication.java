package com.obfuscator;

import com.obfuscator.gui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainApplication extends Application {

    private static final Logger logger = LogManager.getLogger(MainApplication.class);
    private static ObfuscationService obfuscationService;

    public static void main(String[] args) {
        try {
            logger.info("Starting Java Obfuscator application");

            if (args.length > 0) {
                obfuscationService = new ObfuscationService();
                runCLI(args);
            } else {
                logger.info("Launching GUI mode");
                launch(args);
            }

        } catch (Exception e) {
            logger.error("Fatal error during application startup: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runCLI(String[] args) {
        try {

        } catch (Exception e) {
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Initializing GUI");

            obfuscationService = new ObfuscationService();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setObfuscationService(obfuscationService);

            Scene scene = new Scene(root, 900, 700);

            String cssPath = getClass().getResource("/application.css") != null
                    ? getClass().getResource("/application.css").toExternalForm()
                    : null;

            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
                logger.info("CSS loaded: {}", cssPath);
            } else {
                logger.warn("CSS file not found");
            }

            String appVersion = System.getProperty("app.version", "1.0.0");
            primaryStage.setTitle("Java Code Obfuscator v" + appVersion);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Application closing");
                System.exit(0);
            });

            primaryStage.show();
            logger.info("GUI initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize GUI: {}", e.getMessage(), e);
            e.printStackTrace();
            showErrorDialog("GUI Initialization Error",
                    "Failed to initialize graphical interface:\n" + e.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        System.err.println(title + ": " + message);
    }
}