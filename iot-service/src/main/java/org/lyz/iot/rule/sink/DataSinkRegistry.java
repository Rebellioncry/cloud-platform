package org.lyz.iot.rule.sink;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DataSinkRegistry {

    private final Map<String, DataSinkFactory> factories = new ConcurrentHashMap<>();

    public DataSinkRegistry(List<DataSinkFactory> factoryList) {
        for (DataSinkFactory factory : factoryList) {
            factories.put(factory.getType(), factory);
            log.info("注册DataSinkFactory: {}", factory.getType());
        }
    }

    public DataSink getSink(String type, DataSinkConfig config) {
        DataSinkFactory factory = factories.get(type);
        if (factory == null) {
            log.warn("未找到DataSinkFactory: {}", type);
            return null;
        }
        return factory.getInstance(config);
    }

    public DataSinkFactory getFactory(String type) {
        return factories.get(type);
    }

    public Map<String, DataSinkFactory> getAllFactories() {
        return Map.copyOf(factories);
    }
}
