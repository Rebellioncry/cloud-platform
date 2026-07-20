package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaDataSinkFactory implements DataSinkFactory {

    @Override
    public String getType() {
        return "kafka";
    }

    @Override
    public DataSink getInstance(DataSinkConfig config) {
        KafkaDataSink sink = new KafkaDataSink();
        sink.init(config);
        return sink;
    }
}
