package org.lyz.iot.rule.engine;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotRule;
import org.lyz.iot.dao.IotRuleDao;
import org.lyz.iot.entity.IotRuleExecLog;
import org.lyz.iot.service.RuleLogService;
import org.lyz.iot.rule.model.RuleData;
import org.lyz.iot.rule.model.RuleModel;
import org.lyz.iot.rule.model.RuleNodeModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngineService {

    private final RuleDataBus ruleDataBus;
    private final FlowJsonConverter flowJsonConverter;
    private final TaskExecutorRegistry taskExecutorRegistry;
    private final IotRuleDao ruleDao;
    private final RuleLogService ruleLogService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            8, 32, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    private final Map<String, RuleModel> runningRules = new ConcurrentHashMap<>();

    public void start() {
        ruleDataBus.subscribe(this::onRuleEvent);
        log.info("RuleEngineService已启动, 监听RuleDataBus, 线程池: core=8, max=32, queue=1000");
    }

    public void loadRule(IotRule rule) {
        if (rule.getRuleModel() == null || rule.getRuleModel().isBlank()) {
            log.warn("规则模型为空, 跳过加载: {}", rule.getId());
            return;
        }
        try {
            RuleModel model = flowJsonConverter.convert(rule.getRuleModel(), rule.getId(), rule.getName());
            runningRules.put(rule.getId(), model);
            log.info("规则已加载: {} - {}", rule.getId(), rule.getName());
            log.debug("加载后节点IDs: {}", model.getNodes().stream()
                    .map(n -> n.getId() + "[" + n.getType() + "]")
                    .reduce((a, b) -> a + ", " + b).orElse("无"));
            log.debug("加载后连接: {}", model.getLinks().stream()
                    .map(l -> l.getSourceId() + " -> " + l.getTargetId())
                    .reduce((a, b) -> a + ", " + b).orElse("无"));
        } catch (Exception e) {
            log.error("规则加载失败: {}", rule.getId(), e);
        }
    }

    public void unloadRule(String ruleId) {
        runningRules.remove(ruleId);
        log.info("规则已卸载: {}", ruleId);
    }

    private void onRuleEvent(RuleData ruleData) {
        log.debug("RuleDataBus事件接收: productKey={}, deviceName={}, eventType={}, runningRules={}",
                ruleData.getString("productKey"), ruleData.getString("deviceName"),
                ruleData.getString("eventType"), runningRules.size());
        for (Map.Entry<String, RuleModel> entry : runningRules.entrySet()) {
            RuleModel model = entry.getValue();
            RuleNodeModel triggerNode = model.getTriggerNode();
            if (triggerNode == null) {
                log.warn("规则无触发器节点: ruleId={}", model.getId());
                continue;
            }

            boolean matched = matchTrigger(triggerNode, ruleData);

            if (matched) {
                log.info("规则匹配并提交执行: ruleId={}, ruleName={}", model.getId(), model.getName());
                RuleData copied = ruleData.copy();
                copied.setRuleId(model.getId());
                copied.setRuleName(model.getName());
                copied.setTriggerNodeId(triggerNode.getId());
                executorService.submit(() -> {
                    try {
                        executeRuleChain(model, triggerNode.getId(), copied);
                    } catch (Exception e) {
                        log.error("规则异步执行异常: ruleId={}", model.getId(), e);
                    }
                });
            }
        }
    }

    private boolean matchTrigger(RuleNodeModel triggerNode, RuleData ruleData) {
        String eventType = triggerNode.getStringProperty("eventType", "");
        String productKey = ruleData.getString("productKey");
        String triggerProductKey = triggerNode.getStringProperty("productKey", "");

        if (!triggerProductKey.isEmpty() && !triggerProductKey.equals(productKey)) {
            return false;
        }

        if (!eventType.isEmpty()) {
            String dataEventType = ruleData.getString("eventType");
            if (!eventType.equals(dataEventType)) {
                return false;
            }
        }

        return true;
    }

    private void executeRuleChain(RuleModel model, String nodeId, RuleData ruleData) {
        List<RuleNodeModel> nextNodes = model.getNextNodes(nodeId);
        log.debug("执行链路查询: ruleId={}, fromNodeId={}, nextNodesCount={}", model.getId(), nodeId, nextNodes.size());
        if (nextNodes.isEmpty()) {
            log.debug("规则链路终止: ruleId={}, nodeId={}", model.getId(), nodeId);
            return;
        }

        for (RuleNodeModel nextNode : nextNodes) {
            CompletableFuture<RuleData> future = executeNode(nextNode, ruleData);
            future.thenAccept(resultData -> {
                if (!resultData.isStopped()) {
                    incrementMatchCount(model.getId());
                    executeRuleChain(model, nextNode.getId(), resultData);
                } else {
                    log.info("规则链路被条件终止: ruleId={}, nodeId={}", model.getId(), nextNode.getId());
                }
            }).exceptionally(ex -> {
                log.error("规则节点执行异常: ruleId={}, nodeId={}", model.getId(), nextNode.getId(), ex);
                return null;
            });
        }
    }

    private CompletableFuture<RuleData> executeNode(RuleNodeModel nodeModel, RuleData ruleData) {
        log.debug("开始执行节点: ruleId={}, nodeId={}, type={}",
                ruleData.getRuleId(), nodeModel.getId(), nodeModel.getType());
        TaskExecutor executor = taskExecutorRegistry.createExecutor(nodeModel.getType(), nodeModel);
        if (executor == null) {
            log.warn("未找到节点执行器: type={}, nodeId={}", nodeModel.getType(), nodeModel.getId());
            return CompletableFuture.completedFuture(ruleData);
        }

        ruleData.setCurrentNodeId(nodeModel.getId());
        long startTime = System.currentTimeMillis();

        return executor.execute(ruleData).whenComplete((result, ex) -> {
            long duration = System.currentTimeMillis() - startTime;
            IotRuleExecLog execLog = new IotRuleExecLog();
            execLog.setRuleId(ruleData.getRuleId());
            execLog.setRuleName(ruleData.getRuleName());
            execLog.setNodeId(nodeModel.getId());
            execLog.setNodeName(nodeModel.getStringProperty("name", nodeModel.getType()));
            execLog.setNodeType(nodeModel.getType());
            execLog.setDuration(duration);
            execLog.setDeviceKey(ruleData.getString("deviceName"));
            execLog.setProductKey(ruleData.getString("productKey"));
            execLog.setExecuteTime(LocalDateTime.now());
            execLog.setInputData(serializePayload(ruleData.get("payload")));
            execLog.setStatus(ex != null || (result != null && result.isStopped()) ? 1 : 0);
            if (ex != null) {
                execLog.setErrorMessage(ex.getMessage());
            }
            ruleLogService.saveLog(execLog);
        });
    }

    private void incrementMatchCount(String ruleId) {
        try {
            LambdaUpdateWrapper<IotRule> update = new LambdaUpdateWrapper<>();
            update.eq(IotRule::getId, ruleId)
                  .setSql("match_count = match_count + 1")
                  .set(IotRule::getLastExecuteTime, LocalDateTime.now());
            ruleDao.updateIgnoreTenant(null, update);
        } catch (Exception e) {
            log.error("更新规则匹配次数失败: {}", ruleId, e);
        }
    }

    private String serializePayload(Object payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("序列化payload失败, 回退toString: {}", e.getMessage());
            return payload.toString();
        }
    }

    public boolean isRuleRunning(String ruleId) {
        return runningRules.containsKey(ruleId);
    }

    public int getRunningRuleCount() {
        return runningRules.size();
    }

    public void triggerTest(String ruleId, String productKey, String deviceName) {
        RuleModel model = runningRules.get(ruleId);
        if (model == null) {
            log.warn("规则未运行, 无法测试触发: ruleId={}", ruleId);
            throw new RuntimeException("规则未运行: " + ruleId);
        }
        RuleNodeModel triggerNode = model.getTriggerNode();
        if (triggerNode == null) {
            log.warn("规则无触发器节点: ruleId={}", ruleId);
            throw new RuntimeException("规则无触发器节点: " + ruleId);
        }

        RuleData ruleData = new RuleData();
        ruleData.put("topic", "sys/" + productKey + "/" + deviceName + "/thing/property/post");
        ruleData.put("normalizedTopic", productKey + "/" + deviceName + "/thing/property/post");
        ruleData.put("productKey", productKey);
        ruleData.put("deviceName", deviceName);
        ruleData.put("method", "thing.property.post");
        ruleData.put("eventType", "property");
        ruleData.put("payload", Map.of(
            "method", "thing.property.post",
            "id", "test-001",
            "params", Map.of("temperature", 25.5, "humidity", 60)
        ));

        log.info("手动触发测试: ruleId={}, productKey={}, deviceName={}", ruleId, productKey, deviceName);
        ruleData.setRuleId(model.getId());
        ruleData.setRuleName(model.getName());
        ruleData.setTriggerNodeId(triggerNode.getId());
        executeRuleChain(model, triggerNode.getId(), ruleData);
    }
}
