package org.lyz.simulator.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lyz.simulator.config.AppConfig;
import org.lyz.simulator.config.LiveConfig;
import org.lyz.simulator.db.DeviceRepository;
import org.lyz.simulator.model.DeviceInfo;
import org.lyz.simulator.model.ThingModel;
import org.lyz.simulator.mqtt.MqttSimulator;
import org.lyz.simulator.mqtt.TopicUtils;
import org.lyz.simulator.sim.OtaSimulator;
import org.lyz.simulator.sim.PropertySimulator;
import org.lyz.simulator.sim.ServiceCallHandler;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class SimulatorEngine {

    private final AppConfig appConfig;
    private final LiveConfig liveConfig;
    private final DeviceRepository deviceRepository;
    private final MqttSimulator mqtt;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> propertyTask;
    private ScheduledFuture<?> otaVersionTask;

    private DeviceInfo currentDevice;
    private PropertySimulator propertySimulator;
    private OtaSimulator otaSimulator;
    private ServiceCallHandler serviceCallHandler;

    private Consumer<String> logCallback;
    private Runnable onConnected;
    private Runnable onDisconnected;
    private Runnable onDeviceLoaded;
    private Consumer<Boolean> onOtaUpgrading;

    public SimulatorEngine(AppConfig appConfig, LiveConfig liveConfig) {
        this.appConfig = appConfig;
        this.liveConfig = liveConfig;
        this.deviceRepository = new DeviceRepository(appConfig);
        this.mqtt = new MqttSimulator();
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "engine-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });

        mqtt.addListener(this::handleMqttMessage);
    }

    public void setLogCallback(Consumer<String> cb) { this.logCallback = cb; }
    public void setOnConnected(Runnable cb) { this.onConnected = cb; }
    public void setOnDisconnected(Runnable cb) { this.onDisconnected = cb; }
    public void setOnDeviceLoaded(Runnable cb) { this.onDeviceLoaded = cb; }
    public void setOnOtaUpgrading(Consumer<Boolean> cb) { this.onOtaUpgrading = cb; }

    public void connect(String deviceName) {
        new Thread(() -> {
            try {
                log("正在加载设备信息: " + deviceName);
                DeviceInfo device = deviceRepository.loadDevice(deviceName);
                if (device == null) {
                    log("设备不存在: " + deviceName);
                    return;
                }
                device.setThingModel(ThingModel.parse(device.getThingModelJson()));
                this.currentDevice = device;
                log("设备加载成功: productKey=" + device.getProductKey() + ", deviceKey=" + device.getDeviceKey().substring(0, 8) + "...");

                if (device.getThingModel() != null) {
                    log("物模型: " + device.getThingModel().getProperties().size() + "个属性, "
                            + device.getThingModel().getServices().size() + "个服务, "
                            + device.getThingModel().getEvents().size() + "个事件");
                }

                this.propertySimulator = new PropertySimulator(mqtt, liveConfig, device);
                this.otaSimulator = new OtaSimulator(mqtt, liveConfig, device);
                this.serviceCallHandler = new ServiceCallHandler(mqtt, device);

                log("正在连接MQTT: " + appConfig.getMqttBroker() + ":" + appConfig.getMqttPort());
                mqtt.connect(appConfig.getMqttBroker(), appConfig.getMqttPort(), device.getDeviceName(), device.getDeviceKey());
                log("MQTT连接成功");

                liveConfig.setDeviceName(deviceName);
                startScheduling();

                if (onConnected != null) onConnected.run();
                if (onDeviceLoaded != null) onDeviceLoaded.run();
            } catch (Exception e) {
                log("连接失败: " + e.getMessage());
                e.printStackTrace();
            }
        }, "connect").start();
    }

    public void disconnect() {
        stopScheduling();
        mqtt.disconnect();
        this.currentDevice = null;
        if (onDisconnected != null) onDisconnected.run();
        log("已断开连接");
    }

    private void startScheduling() {
        stopScheduling();
        propertyTask = scheduler.scheduleAtFixedRate(() -> {
            if (propertySimulator != null && liveConfig.isPropertyReportEnabled()) {
                propertySimulator.report();
            }
        }, 2, liveConfig.getPropertyInterval(), TimeUnit.MILLISECONDS);

        if (liveConfig.isOtaEnabled()) {
            otaVersionTask = scheduler.scheduleAtFixedRate(() -> {
                if (otaSimulator != null) {
                    otaSimulator.informVersion();
                }
            }, 5, liveConfig.getOtaVersionInterval(), TimeUnit.MILLISECONDS);
        }
    }

    public void rescheduleProperty() {
        if (propertyTask != null) {
            propertyTask.cancel(false);
        }
        if (mqtt.isConnected()) {
            propertyTask = scheduler.scheduleAtFixedRate(() -> {
                if (propertySimulator != null && liveConfig.isPropertyReportEnabled()) {
                    propertySimulator.report();
                }
            }, 0, liveConfig.getPropertyInterval(), TimeUnit.MILLISECONDS);
            log("属性上报间隔已更新: " + liveConfig.getPropertyInterval() + "ms");
        }
    }

    public void rescheduleOtaVersion() {
        if (otaVersionTask != null) {
            otaVersionTask.cancel(false);
        }
        if (mqtt.isConnected() && liveConfig.isOtaEnabled()) {
            otaVersionTask = scheduler.scheduleAtFixedRate(() -> {
                if (otaSimulator != null) {
                    otaSimulator.informVersion();
                }
            }, 0, liveConfig.getOtaVersionInterval(), TimeUnit.MILLISECONDS);
            log("OTA版本上报已恢复: 间隔=" + liveConfig.getOtaVersionInterval() + "ms");
        } else if (!liveConfig.isOtaEnabled()) {
            log("OTA版本上报已停止");
        }
    }

    private void stopScheduling() {
        if (propertyTask != null) { propertyTask.cancel(true); propertyTask = null; }
        if (otaVersionTask != null) { otaVersionTask.cancel(true); otaVersionTask = null; }
    }

    private void handleMqttMessage(String topic, String payload) {
        try {
            if (topic.contains("/thing/service/property/set")) {
                serviceCallHandler.handlePropertySet(topic, payload);
                log("收到属性下发: " + payload);
            } else if (topic.contains("/thing/service/") && topic.contains("/invoke")) {
                serviceCallHandler.handleInvoke(topic, payload);
                log("收到服务调用: " + payload);
            } else if (topic.contains("ota/device/upgrade/")) {
                otaSimulator.handleUpgrade(payload);
                log("收到OTA升级指令: " + payload);
                if (onOtaUpgrading != null) {
                    onOtaUpgrading.accept(true);
                }
            }
        } catch (Exception e) {
            log("消息处理异常: " + e.getMessage());
        }
    }

    private void log(String msg) {
        String time = java.time.LocalTime.now().toString().substring(0, 8);
        String line = "[" + time + "] " + msg;
        System.out.println(line);
        if (logCallback != null) logCallback.accept(line);
    }

    public DeviceInfo getCurrentDevice() { return currentDevice; }
    public boolean isConnected() { return mqtt.isConnected(); }
    public PropertySimulator getPropertySimulator() { return propertySimulator; }
    public OtaSimulator getOtaSimulator() { return otaSimulator; }
}
