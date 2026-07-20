package org.lyz.iot.rule.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RuleData {

    private String ruleId;
    private String ruleName;
    private String triggerNodeId;
    private String currentNodeId;
    private Map<String, Object> data = new HashMap<>();
    private long timestamp;
    private boolean stopped;

    public RuleData() {
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
    }

    public RuleData(String ruleId) {
        this();
        this.ruleId = ruleId;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = data.get(key);
        if (value == null) return null;
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> getAll() {
        return new HashMap<>(data);
    }

    public RuleData copy() {
        RuleData copy = new RuleData(this.ruleId);
        copy.ruleName = this.ruleName;
        copy.triggerNodeId = this.triggerNodeId;
        copy.currentNodeId = this.currentNodeId;
        copy.stopped = this.stopped;
        copy.timestamp = this.timestamp;
        copy.data = new HashMap<>(this.data);
        return copy;
    }
}
