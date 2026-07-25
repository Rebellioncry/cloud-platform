package org.lyz.simulator.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.concurrent.CopyOnWriteArrayList;

public class SimulatorLayout {

    public final TextField brokerField = new TextField("172.26.76.190");
    public final TextField portField = new TextField("1883");
    public final TextField deviceNameField = new TextField("test1");
    public final Button connectBtn = new Button("连接");
    public final Button disconnectBtn = new Button("断开");

    public final Label statusLabel = new Label("● 未连接");
    public final Label productKeyLabel = new Label("-");
    public final Label deviceKeyLabel = new Label("-");
    public final Label fwVersionLabel = new Label("-");

    public final TextField propertyIntervalField = new TextField("5000");
    public final ToggleButton propertyReportToggle = new ToggleButton("属性上报");
    public final CheckBox otaEnabledBox = new CheckBox("启用OTA");
    public final TextField firmwareVersionField = new TextField("1.0.0");
    public final TextField otaVersionIntervalField = new TextField("30000");
    public final Button applyBtn = new Button("应用设置");
    public final Button pullBtn = new Button("主动拉取");

    public final TableView<ObservableList<String>> propertyTable = new TableView<>();
    public final ObservableList<ObservableList<String>> propertyData = FXCollections.observableArrayList();

    public final TextArea logArea = new TextArea();
    public final Label reportCountLabel = new Label("上报次数: 0");

    private final CopyOnWriteArrayList<String> logLines = new CopyOnWriteArrayList<>();
    private static final int MAX_LOG_LINES = 500;

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e2a3a;");

        root.setTop(buildTopBar());
        root.setCenter(buildCenter());
        root.setBottom(buildLogPanel());

        return new Scene(root, 1000, 720);
    }

    private VBox buildTopBar() {
        VBox top = new VBox(8);
        top.setPadding(new Insets(12));
        top.setStyle("-fx-background-color: #253545;");

        HBox connRow = new HBox(8);
        connRow.setAlignment(Pos.CENTER_LEFT);
        Label brokerLabel = createLabel("MQTT:");
        brokerField.setPrefWidth(140);
        brokerField.setStyle("-fx-background-color: #1a2a3a; -fx-text-fill: #e0e0e0; -fx-border-color: #3a5a7a;");
        portField.setPrefWidth(60);
        portField.setStyle("-fx-background-color: #1a2a3a; -fx-text-fill: #e0e0e0; -fx-border-color: #3a5a7a;");
        Label devLabel = createLabel("设备:");
        deviceNameField.setPrefWidth(120);
        deviceNameField.setStyle("-fx-background-color: #1a2a3a; -fx-text-fill: #e0e0e0; -fx-border-color: #3a5a7a;");
        connectBtn.setStyle("-fx-background-color: #2d7d46; -fx-text-fill: white; -fx-font-weight: bold;");
        disconnectBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        disconnectBtn.setDisable(true);
        connRow.getChildren().addAll(brokerLabel, brokerField, portField, devLabel, deviceNameField, connectBtn, disconnectBtn);

        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusLabel.setTextFill(Color.web("#e74c3c"));
        statusLabel.setFont(Font.font("Consolas", FontWeight.NORMAL, 13));
        Label pkTitle = createLabel("产品Key:");
        productKeyLabel.setTextFill(Color.web("#3498db"));
        productKeyLabel.setFont(Font.font("Consolas", 12));
        Label dkTitle = createLabel("设备Key:");
        deviceKeyLabel.setTextFill(Color.web("#3498db"));
        deviceKeyLabel.setFont(Font.font("Consolas", 12));
        Label fwTitle = createLabel("固件版本:");
        fwVersionLabel.setTextFill(Color.web("#2ecc71"));
        fwVersionLabel.setFont(Font.font("Consolas", 12));
        statusRow.getChildren().addAll(statusLabel, pkTitle, productKeyLabel, dkTitle, deviceKeyLabel, fwTitle, fwVersionLabel);

        top.getChildren().addAll(connRow, statusRow);
        return top;
    }

    private VBox buildCenter() {
        VBox center = new VBox(8);
        center.setPadding(new Insets(8));

        TitledPane configPane = buildConfigPane();
        TitledPane propPane = buildPropertyPane();
        VBox.setVgrow(propPane, Priority.ALWAYS);
        center.getChildren().addAll(configPane, propPane);
        return center;
    }

    private TitledPane buildConfigPane() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));

        Label intLabel = createLabel("上报间隔(ms):");
        propertyIntervalField.setPrefWidth(80);
        styleField(propertyIntervalField);
        propertyReportToggle.setSelected(true);
        propertyReportToggle.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        otaEnabledBox.setSelected(true);
        otaEnabledBox.setTextFill(Color.web("#e0e0e0"));
        Label fwLabel = createLabel("固件版本:");
        firmwareVersionField.setPrefWidth(80);
        styleField(firmwareVersionField);
        Label otaIntLabel = createLabel("OTA版本间隔(ms):");
        otaVersionIntervalField.setPrefWidth(80);
        styleField(otaVersionIntervalField);
        applyBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        pullBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        pullBtn.setDisable(true);
        reportCountLabel.setTextFill(Color.web("#95a5a6"));
        reportCountLabel.setFont(Font.font("Consolas", 11));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(intLabel, propertyIntervalField, propertyReportToggle, otaEnabledBox, fwLabel, firmwareVersionField,
                otaIntLabel, otaVersionIntervalField, applyBtn, pullBtn, spacer, reportCountLabel);

        TitledPane pane = new TitledPane("模拟控制", row);
        pane.setCollapsible(false);
        pane.setStyle("-fx-text-fill: #ecf0f1; -fx-border-color: #3a5a7a;");
        return pane;
    }

    private TitledPane buildPropertyPane() {
        TableColumn<ObservableList<String>, String> idCol = new TableColumn<>("标识符");
        idCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(0)));
        idCol.setPrefWidth(100);

        TableColumn<ObservableList<String>, String> nameCol = new TableColumn<>("名称");
        nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(1)));
        nameCol.setPrefWidth(100);

        TableColumn<ObservableList<String>, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(2)));
        typeCol.setPrefWidth(80);

        TableColumn<ObservableList<String>, String> rangeCol = new TableColumn<>("范围");
        rangeCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(3)));
        rangeCol.setPrefWidth(120);

        TableColumn<ObservableList<String>, String> lastValCol = new TableColumn<>("上次值");
        lastValCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(4)));
        lastValCol.setPrefWidth(100);

        TableColumn<ObservableList<String>, String> countCol = new TableColumn<>("上报次数");
        countCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(5)));
        countCol.setPrefWidth(80);

        propertyTable.getColumns().addAll(idCol, nameCol, typeCol, rangeCol, lastValCol, countCol);
        propertyTable.setItems(propertyData);
        propertyTable.setStyle("-fx-background-color: #1a2a3a; -fx-table-cell-border-color: #2a3a4a;");
        propertyTable.setPlaceholder(new Label("请先连接设备"));

        TitledPane pane = new TitledPane("属性定义", propertyTable);
        pane.setCollapsible(false);
        pane.setStyle("-fx-text-fill: #ecf0f1; -fx-border-color: #3a5a7a;");
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }

    private VBox buildLogPanel() {
        logArea.setEditable(false);
        logArea.setPrefHeight(180);
        logArea.setFont(Font.font("Consolas", 12));
        logArea.setStyle("-fx-background-color: #0d1117; -fx-text-fill: #8b949e; -fx-control-inner-background: #0d1117;");

        TitledPane pane = new TitledPane("日志", logArea);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        pane.setStyle("-fx-text-fill: #ecf0f1; -fx-border-color: #3a5a7a;");
        VBox.setVgrow(pane, Priority.SOMETIMES);

        VBox bottom = new VBox(pane);
        bottom.setPadding(new Insets(0, 8, 8, 8));
        return bottom;
    }

    public void appendLog(String line) {
        logLines.add(line);
        while (logLines.size() > MAX_LOG_LINES) {
            logLines.remove(0);
        }
        Platform.runLater(() -> {
            logArea.appendText(line + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void setStatus(boolean connected, String productKey, String deviceKey, String fwVersion) {
        Platform.runLater(() -> {
            if (connected) {
                statusLabel.setText("● 已连接");
                statusLabel.setTextFill(Color.web("#2ecc71"));
                connectBtn.setDisable(true);
                disconnectBtn.setDisable(false);
            } else {
                statusLabel.setText("● 未连接");
                statusLabel.setTextFill(Color.web("#e74c3c"));
                connectBtn.setDisable(false);
                disconnectBtn.setDisable(true);
            }
            productKeyLabel.setText(productKey != null ? productKey : "-");
            deviceKeyLabel.setText(deviceKey != null ? (deviceKey.length() > 12 ? deviceKey.substring(0, 12) + "..." : deviceKey) : "-");
            fwVersionLabel.setText(fwVersion != null ? fwVersion : "-");
        });
    }

    public void updatePropertyTable(org.lyz.simulator.model.ThingModel model) {
        Platform.runLater(() -> {
            propertyData.clear();
            if (model == null) return;
            for (org.lyz.simulator.model.ThingModel.PropertyDef prop : model.getProperties()) {
                String range = "";
                if ("int32".equals(prop.dataType) || "float".equals(prop.dataType) || "double".equals(prop.dataType)) {
                    range = prop.min + " ~ " + prop.max;
                } else if ("enum".equals(prop.dataType) && prop.enumValues != null) {
                    range = String.join(", ", prop.enumValues.keySet());
                }
                propertyData.add(FXCollections.observableArrayList(
                        prop.identifier, prop.name, prop.dataType, range, "-", "0"));
            }
        });
    }

    public void updateReportCount(long count) {
        Platform.runLater(() -> reportCountLabel.setText("上报次数: " + count));
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#bdc3c7"));
        l.setFont(Font.font("System", FontWeight.NORMAL, 12));
        return l;
    }

    private void styleField(TextField field) {
        field.setStyle("-fx-background-color: #1a2a3a; -fx-text-fill: #e0e0e0; -fx-border-color: #3a5a7a;");
    }

    public void setConnected(boolean connected) {
        Platform.runLater(() -> {
            connectBtn.setDisable(connected);
            disconnectBtn.setDisable(!connected);
        });
    }
}
