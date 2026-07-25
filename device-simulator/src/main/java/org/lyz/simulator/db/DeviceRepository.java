package org.lyz.simulator.db;

import org.lyz.simulator.config.AppConfig;
import org.lyz.simulator.model.DeviceInfo;

import java.sql.*;

public class DeviceRepository {

    private final AppConfig config;

    public DeviceRepository(AppConfig config) {
        this.config = config;
    }

    public DeviceInfo loadDevice(String deviceName) throws SQLException {
        String sql = "SELECT d.id, d.product_key, d.device_name, d.device_key, d.firmware_version, "
                + "p.thing_model "
                + "FROM iot_device d "
                + "JOIN iot_product p ON d.product_key = p.product_key "
                + "WHERE d.device_name = ? AND d.deleted = 0 LIMIT 1";

        try (Connection conn = DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deviceName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DeviceInfo info = new DeviceInfo();
                info.setId(rs.getString("id"));
                info.setProductKey(rs.getString("product_key"));
                info.setDeviceName(rs.getString("device_name"));
                info.setDeviceKey(rs.getString("device_key"));
                info.setFirmwareVersion(rs.getString("firmware_version"));
                info.setThingModelJson(rs.getString("thing_model"));
                return info;
            }
        }
    }
}
