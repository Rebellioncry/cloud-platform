package org.lyz.iot.rule.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RuleNodeModel {

    private String id;
    private String name;
    private String type;
    private String nodeType;
    private Map<String, Object> properties = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> clazz) {
        Object value = properties.get(key);
        if (value == null) return null;
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public String getStringProperty(String key, String defaultValue) {
        Object value = properties.get(key);
        if (value == null) return defaultValue;
        return value.toString();
    }

    public Integer getIntProperty(String key, Integer defaultValue) {
        Object value = properties.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean isTrigger() {
        return "trigger".equals(nodeType);
    }

    public boolean isTransform() {
        return "transform".equals(nodeType);
    }

    public boolean isAction() {
        return "action".equals(nodeType);
    }

    public boolean isSink() {
        return "sink".equals(nodeType);
    }
}
