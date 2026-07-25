package org.lyz.simulator.gui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lyz.simulator.config.AppConfig;
import org.lyz.simulator.config.LiveConfig;
import org.lyz.simulator.engine.SimulatorEngine;

public class SimulatorController {

    private final SimulatorEngine engine;
    private final SimulatorLayout layout;
    private final LiveConfig liveConfig;
    private final Stage stage;
    private java.util.Timer uiRefreshTimer;

    public SimulatorController(Stage stage, AppConfig appConfig) {
        this.stage = stage;
        this.liveConfig = new LiveConfig();
        this.engine = new SimulatorEngine(appConfig, liveConfig);
        this.layout = new SimulatorLayout();

        bindCallbacks();
        bindEvents();
    }

    private void bindCallbacks() {
        engine.setLogCallback(line -> layout.appendLog(line));

        engine.setOnConnected(() -> {
            layout.setStatus(true, null, null, null);
            layout.pullBtn.setDisable(false);
        });

        engine.setOnDisconnected(() -> {
            layout.setStatus(false, null, null, null);
            layout.pullBtn.setDisable(true);
            stopUiRefresh();
        });

        engine.setOnDeviceLoaded(() -> {
            var device = engine.getCurrentDevice();
            if (device != null) {
                layout.setStatus(true, device.getProductKey(), device.getDeviceKey(), liveConfig.getFirmwareVersion());
                layout.updatePropertyTable(device.getThingModel());
                layout.firmwareVersionField.setText(liveConfig.getFirmwareVersion());
                startUiRefresh();
            }
        });
    }

    private void bindEvents() {
        layout.connectBtn.setOnAction(e -> {
            String deviceName = layout.deviceNameField.getText().trim();
            if (deviceName.isEmpty()) {
                layout.appendLog("请输入设备名称");
                return;
            }
            liveConfig.setDeviceName(deviceName);
            engine.connect(deviceName);
        });

        layout.disconnectBtn.setOnAction(e -> {
            engine.disconnect();
            layout.updatePropertyTable(null);
            stopUiRefresh();
        });

        layout.pullBtn.setOnAction(e -> {
            if (engine.getOtaSimulator() != null) {
                engine.getOtaSimulator().informVersion();
                layout.appendLog("主动拉取: 已发送固件版本上报");
            }
        });

        layout.propertyReportToggle.setOnAction(e -> {
            boolean enabled = layout.propertyReportToggle.isSelected();
            liveConfig.setPropertyReportEnabled(enabled);
            if (enabled) {
                layout.propertyReportToggle.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                layout.appendLog("属性上报已开启");
            } else {
                layout.propertyReportToggle.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold;");
                layout.appendLog("属性上报已暂停");
            }
        });

        layout.applyBtn.setOnAction(e -> {
            try {
                int interval = Integer.parseInt(layout.propertyIntervalField.getText().trim());
                liveConfig.setPropertyInterval(interval);
                engine.rescheduleProperty();
                layout.appendLog("属性上报间隔已更新: " + interval + "ms");
            } catch (NumberFormatException ex) {
                layout.appendLog("上报间隔格式错误");
            }

            liveConfig.setOtaEnabled(layout.otaEnabledBox.isSelected());
            engine.rescheduleOtaVersion();

            String fwVersion = layout.firmwareVersionField.getText().trim();
            if (!fwVersion.isEmpty()) {
                liveConfig.setFirmwareVersion(fwVersion);
                layout.appendLog("固件版本已更新: " + fwVersion);
            }

            try {
                int otaInterval = Integer.parseInt(layout.otaVersionIntervalField.getText().trim());
                liveConfig.setOtaVersionInterval(otaInterval);
                engine.rescheduleOtaVersion();
            } catch (NumberFormatException ex) {
                layout.appendLog("OTA间隔格式错误");
            }
        });

        stage.setOnCloseRequest(e -> {
            engine.disconnect();
            stopUiRefresh();
            Platform.exit();
            System.exit(0);
        });
    }

    private void startUiRefresh() {
        stopUiRefresh();
        uiRefreshTimer = new java.util.Timer("ui-refresh", true);
        uiRefreshTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (engine.getPropertySimulator() != null) {
                    layout.updateReportCount(engine.getPropertySimulator().getReportCount());
                }
            }
        }, 1000, 1000);
    }

    private void stopUiRefresh() {
        if (uiRefreshTimer != null) {
            uiRefreshTimer.cancel();
            uiRefreshTimer = null;
        }
    }

    public Scene createScene() {
        return layout.buildScene();
    }

    public SimulatorEngine getEngine() { return engine; }
    public SimulatorLayout getLayout() { return layout; }
}
