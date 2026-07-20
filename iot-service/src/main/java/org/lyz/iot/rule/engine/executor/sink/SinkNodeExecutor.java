package org.lyz.iot.rule.engine.executor.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.engine.TaskExecutor;
import org.lyz.iot.rule.engine.TaskExecutorProvider;
import org.lyz.iot.rule.model.RuleData;
import org.lyz.iot.rule.model.RuleNodeModel;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkMessage;
import org.lyz.iot.rule.sink.DataSinkRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SinkNodeExecutor implements TaskExecutorProvider {

    private final DataSinkRegistry dataSinkRegistry;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> SUPPORTED_TYPES = Set.of(
        "kafka-sink", "rocketmq-sink", "mqtt-sink",
        "http-sink", "pulsar-sink", "redis-sink"
    );

    private static final Map<String, String> TYPE_MAP = Map.of(
        "kafka-sink", "kafka",
        "rocketmq-sink", "rocketmq",
        "mqtt-sink", "mqtt",
        "http-sink", "http",
        "pulsar-sink", "pulsar",
        "redis-sink", "redis"
    );

    @Override
    public String getType() {
        return "sink-bridge";
    }

    @Override
    public boolean supports(String type) {
        return SUPPORTED_TYPES.contains(type);
    }

    @Override
    public TaskExecutor createExecutor(RuleNodeModel nodeModel) {
        return new Executor(nodeModel, dataSinkRegistry);
    }

    private static class Executor implements TaskExecutor {
        private final RuleNodeModel nodeModel;
        private final DataSinkRegistry dataSinkRegistry;
        private final String sinkType;

        public Executor(RuleNodeModel nodeModel, DataSinkRegistry dataSinkRegistry) {
            this.nodeModel = nodeModel;
            this.dataSinkRegistry = dataSinkRegistry;
            this.sinkType = TYPE_MAP.getOrDefault(nodeModel.getType(), "unknown");
        }

        @Override
        public String getType() {
            return nodeModel.getType();
        }

        @Override
        public void init(RuleNodeModel nodeModel) {
        }

        @Override
        public CompletableFuture<RuleData> execute(RuleData ruleData) {
            try {
                DataSinkConfig config = buildConfig(nodeModel);
                log.info("Sink节点开始执行: type={}, sinkType={}, nodeId={}, config={}", nodeModel.getType(), sinkType, nodeModel.getId(), config.getProperties());
                DataSink sink = dataSinkRegistry.getSink(sinkType, config);
                if (sink == null) {
                    log.warn("未找到DataSinkFactory: type={}, nodeId={}", sinkType, nodeModel.getId());
                    ruleData.setStopped(true);
                    return CompletableFuture.completedFuture(ruleData);
                }

                DataSinkMessage message = buildMessage(ruleData);
                log.info("Sink节点发送消息: type={}, topic={}, key={}, payloadLength={}", sinkType, message.getTopic(), message.getKey(), message.getPayload() != null ? message.getPayload().length() : 0);
                sink.send(message);

                log.info("Sink节点执行成功: type={}, nodeId={}", sinkType, nodeModel.getId());
                return CompletableFuture.completedFuture(ruleData);
            } catch (Exception e) {
                log.error("Sink节点执行失败: type={}, nodeId={}", sinkType, nodeModel.getId(), e);
                ruleData.setStopped(true);
                return CompletableFuture.completedFuture(ruleData);
            }
        }

        private DataSinkConfig buildConfig(RuleNodeModel nodeModel) {
            DataSinkConfig config = new DataSinkConfig();
            config.setType(sinkType);
            config.setProperties(new HashMap<>(nodeModel.getProperties()));
            return config;
        }

        private DataSinkMessage buildMessage(RuleData ruleData) {
            DataSinkMessage message = new DataSinkMessage();
            String configTopic = nodeModel.getStringProperty("topic", "");
            String configKey = nodeModel.getStringProperty("key", "");

            message.setTopic(!configTopic.isEmpty() ? configTopic : ruleData.getString("topic"));
            message.setKey(!configKey.isEmpty() ? configKey : ruleData.getString("deviceName"));

            Object payloadObj = ruleData.get("payload");
            String payload;
            if (payloadObj instanceof Map) {
                try {
                    payload = objectMapper.writeValueAsString(payloadObj);
                } catch (Exception e) {
                    payload = payloadObj.toString();
                }
            } else if (payloadObj != null) {
                payload = payloadObj.toString();
            } else {
                try {
                    payload = objectMapper.writeValueAsString(ruleData.getAll());
                } catch (Exception e) {
                    payload = ruleData.getAll().toString();
                }
            }
            message.setPayload(payload);
            return message;
        }
    }
}
