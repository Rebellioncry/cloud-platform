package org.lyz.simulator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lyz.simulator.config.AppConfig;
import org.lyz.simulator.gui.SimulatorController;

public class DeviceSimulator extends Application {

    private static AppConfig appConfig;

    @Override
    public void start(Stage primaryStage) {
        if (appConfig == null) {
            appConfig = AppConfig.load();
        }

        SimulatorController controller = new SimulatorController(primaryStage, appConfig);
        Scene scene = controller.createScene();

        primaryStage.setTitle("IoT 设备模拟器");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        appConfig = AppConfig.load();
        launch(args);
    }
}
