package org.lyz.iot.rule.engine.executor.trigger;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.engine.TaskExecutor;
import org.lyz.iot.rule.engine.TaskExecutorProvider;
import org.lyz.iot.rule.model.RuleData;
import org.lyz.iot.rule.model.RuleNodeModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class DeviceEventTrigger implements TaskExecutorProvider {

    @Override
    public String getType() {
        return "device-event-trigger";
    }

    @Override
    public TaskExecutor createExecutor(RuleNodeModel nodeModel) {
        return new Executor(nodeModel);
    }

    private static class Executor implements TaskExecutor {
        private final RuleNodeModel nodeModel;

        public Executor(RuleNodeModel nodeModel) {
            this.nodeModel = nodeModel;
        }

        @Override
        public String getType() {
            return "device-event-trigger";
        }

        @Override
        public void init(RuleNodeModel nodeModel) {
        }

        @Override
        public CompletableFuture<RuleData> execute(RuleData ruleData) {
            String productKey = ruleData.getString("productKey");
            String deviceName = ruleData.getString("deviceName");
            String eventType = ruleData.getString("eventType");
            log.info("设备事件触发: productKey={}, deviceName={}, eventType={}", productKey, deviceName, eventType);
            return CompletableFuture.completedFuture(ruleData);
        }
    }
}
