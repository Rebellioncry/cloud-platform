package org.lyz.iot.rule.sink;

import lombok.Data;

import java.util.Map;

@Data
public class DataSinkMessage {

    private String topic;
    private String key;
    private String payload;
    private Map<String, Object> headers;

    public DataSinkMessage() {}

    public DataSinkMessage(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public DataSinkMessage(String topic, String key, String payload) {
        this.topic = topic;
        this.key = key;
        this.payload = payload;
    }
}
