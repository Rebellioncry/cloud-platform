package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MqttDataSinkFactory implements DataSinkFactory {

    private final Map<String, MqttDataSink> connectionCache = new ConcurrentHashMap<>();

    @Override
    public String getType() {
        return "mqtt";
    }

    @Override
    public DataSink getInstance(DataSinkConfig config) {
        String cacheKey = buildCacheKey(config);
        MqttDataSink existing = connectionCache.get(cacheKey);
        if (existing != null && existing.isAlive()) {
            log.debug("MQTT DataSink复用连接: {}", cacheKey);
            return existing;
        }
        MqttDataSink sink = new MqttDataSink();
        sink.init(config);
        connectionCache.put(cacheKey, sink);
        log.info("MQTT DataSink创建新连接: {}", cacheKey);
        return sink;
    }

    public void closeAll() {
        connectionCache.values().forEach(MqttDataSink::close);
        connectionCache.clear();
        log.info("所有MQTT DataSink连接已关闭");
    }

    private String buildCacheKey(DataSinkConfig config) {
        String broker = config.getString("broker", "localhost");
        int port = config.getInt("port", 1883);
        String username = config.getString("username", "");
        return broker + ":" + port + ":" + username;
    }
}
