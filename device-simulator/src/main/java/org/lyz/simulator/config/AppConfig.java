package org.lyz.simulator.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private final String mqttBroker;
    private final int mqttPort;
    private final int mqttQos;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private AppConfig(Properties props) {
        this.mqttBroker = props.getProperty("mqtt.broker", "172.26.76.190");
        this.mqttPort = Integer.parseInt(props.getProperty("mqtt.port", "1883"));
        this.mqttQos = Integer.parseInt(props.getProperty("mqtt.qos", "1"));
        this.dbUrl = props.getProperty("db.url", "jdbc:mysql://172.26.76.190:3306/cloud_platform");
        this.dbUser = props.getProperty("db.user", "root");
        this.dbPassword = props.getProperty("db.password", "root123");
    }

    public static AppConfig load() {
        Properties props = new Properties();
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            System.err.println("加载config.properties失败: " + e.getMessage());
        }
        return new AppConfig(props);
    }

    public String getMqttBroker() { return mqttBroker; }
    public int getMqttPort() { return mqttPort; }
    public int getMqttQos() { return mqttQos; }
    public String getDbUrl() { return dbUrl; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }
}
