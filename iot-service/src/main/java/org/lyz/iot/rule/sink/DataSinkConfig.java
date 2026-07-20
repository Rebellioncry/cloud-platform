package org.lyz.iot.rule.sink;

import lombok.Data;

import java.util.Map;

@Data
public class DataSinkConfig {

    private String type;
    private Map<String, Object> properties;

    public String getString(String key, String defaultValue) {
        if (properties == null) return defaultValue;
        Object value = properties.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public Integer getInt(String key, Integer defaultValue) {
        if (properties == null) return defaultValue;
        Object value = properties.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        if (properties == null) return defaultValue;
        Object value = properties.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}
