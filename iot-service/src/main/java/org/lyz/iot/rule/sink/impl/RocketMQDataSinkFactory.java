package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RocketMQDataSinkFactory implements DataSinkFactory {

    @Override
    public String getType() {
        return "rocketmq";
    }

    @Override
    public DataSink getInstance(DataSinkConfig config) {
        RocketMQDataSink sink = new RocketMQDataSink();
        sink.init(config);
        return sink;
    }
}
