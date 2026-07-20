package org.lyz.iot.rule.sink.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDataSinkFactory implements DataSinkFactory {

    private final StringRedisTemplate redisTemplate;

    @Override
    public String getType() {
        return "redis";
    }

    @Override
    public DataSink getInstance(DataSinkConfig config) {
        RedisDataSink sink = new RedisDataSink(redisTemplate);
        sink.init(config);
        return sink;
    }
}
