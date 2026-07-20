package org.lyz.iot.rule.engine;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.model.RuleNodeModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TaskExecutorRegistry {

    private final Map<String, TaskExecutorProvider> providers = new ConcurrentHashMap<>();

    public TaskExecutorRegistry(List<TaskExecutorProvider> providerList) {
        for (TaskExecutorProvider provider : providerList) {
            providers.put(provider.getType(), provider);
            log.info("注册TaskExecutorProvider: {}", provider.getType());
        }
    }

    public TaskExecutorProvider getProvider(String type) {
        return providers.get(type);
    }

    public TaskExecutor createExecutor(String type, RuleNodeModel nodeModel) {
        TaskExecutorProvider provider = providers.get(type);
        if (provider == null) {
            for (TaskExecutorProvider p : providers.values()) {
                if (p.supports(type)) {
                    provider = p;
                    break;
                }
            }
        }
        if (provider == null) {
            log.warn("未找到TaskExecutorProvider: {}", type);
            return null;
        }
        return provider.createExecutor(nodeModel);
    }

    public boolean hasProvider(String type) {
        return providers.containsKey(type);
    }

    public Map<String, TaskExecutorProvider> getAllProviders() {
        return Map.copyOf(providers);
    }
}
