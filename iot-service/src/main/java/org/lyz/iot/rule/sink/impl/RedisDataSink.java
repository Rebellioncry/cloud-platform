package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkMessage;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisDataSink implements DataSink {

    private DataSinkConfig config;
    private StringRedisTemplate redisTemplate;
    private boolean alive = false;

    private String host;
    private int port;
    private String defaultKey;
    private int database;

    public RedisDataSink(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getType() {
        return "redis";
    }

    @Override
    public void init(DataSinkConfig config) {
        this.config = config;
        this.host = config.getString("host", "localhost");
        this.port = config.getInt("port", 6379);
        this.defaultKey = config.getString("key", "iot:data");
        this.database = config.getInt("database", 0);
        this.alive = true;
        log.info("Redis DataSink初始化: host={}, port={}, db={}, defaultKey={}", host, port, database, defaultKey);
    }

    @Override
    public void send(DataSinkMessage message) {
        if (!alive || redisTemplate == null) {
            log.warn("Redis DataSink未就绪, 消息丢弃: key={}", message.getKey());
            return;
        }
        String key = message.getKey() != null && !message.getKey().isEmpty()
                ? message.getKey() : defaultKey;
        try {
            String payload = message.getPayload() != null ? message.getPayload() : "";
            redisTemplate.opsForValue().set(key, payload);
            log.debug("Redis DataSink发送成功: key={}, payloadLength={}", key, payload.length());
        } catch (Exception e) {
            log.error("Redis DataSink发送失败: key={}", key, e);
        }
    }

    @Override
    public void close() {
        this.alive = false;
        log.info("Redis DataSink已关闭: host={}:{}", host, port);
    }

    @Override
    public boolean isAlive() {
        return alive;
    }
}
