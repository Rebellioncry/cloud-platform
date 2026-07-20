package org.lyz.iot.rule.engine.executor.transform;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.engine.TaskExecutor;
import org.lyz.iot.rule.engine.TaskExecutorProvider;
import org.lyz.iot.rule.model.RuleData;
import org.lyz.iot.rule.model.RuleNodeModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class DataTransformNode implements TaskExecutorProvider {

    private static final AviatorEvaluatorInstance AVIATOR = AviatorEvaluator.getInstance();

    @Override
    public String getType() {
        return "data-transform";
    }

    @Override
    public TaskExecutor createExecutor(RuleNodeModel nodeModel) {
        return new Executor(nodeModel);
    }

    private static class Executor implements TaskExecutor {
        private final RuleNodeModel nodeModel;
        private final String sourceField;
        private final String targetField;
        private final String expression;

        public Executor(RuleNodeModel nodeModel) {
            this.nodeModel = nodeModel;
            this.sourceField = nodeModel.getStringProperty("sourceField", "");
            this.targetField = nodeModel.getStringProperty("targetField", "");
            this.expression = nodeModel.getStringProperty("expression", "");
        }

        @Override
        public String getType() {
            return "data-transform";
        }

        @Override
        public void init(RuleNodeModel nodeModel) {
        }

        @Override
        public CompletableFuture<RuleData> execute(RuleData ruleData) {
            if (expression.isEmpty()) {
                return CompletableFuture.completedFuture(ruleData);
            }

            try {
                Map<String, Object> env = new HashMap<>(ruleData.getAll());

                Object sourceValue = null;
                if (sourceField != null && !sourceField.isEmpty()) {
                    sourceValue = AVIATOR.execute(sourceField, env);
                }
                env.put("sourceValue", sourceValue);

                Object result = AVIATOR.execute(expression, env);

                if (targetField != null && !targetField.isEmpty()) {
                    setNestedField(ruleData.getAll(), targetField, result);
                    log.info("数据转换完成: {} -> {} = {}", sourceField, targetField, result);
                } else {
                    ruleData.put("transformResult", result);
                    log.info("数据转换完成: result={}", result);
                }

                return CompletableFuture.completedFuture(ruleData);
            } catch (Exception e) {
                log.error("数据转换节点执行失败: sourceField={}, expression={}", sourceField, expression, e);
                ruleData.setStopped(true);
                return CompletableFuture.completedFuture(ruleData);
            }
        }

        private void setNestedField(Map<String, Object> map, String path, Object value) {
            String[] parts = parsePath(path);
            Map<String, Object> current = map;
            for (int i = 0; i < parts.length - 1; i++) {
                Object next = current.get(parts[i]);
                if (next instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nextMap = (Map<String, Object>) next;
                    current = nextMap;
                } else {
                    Map<String, Object> newMap = new HashMap<>();
                    current.put(parts[i], newMap);
                    current = newMap;
                }
            }
            current.put(parts[parts.length - 1], value);
        }

        private String[] parsePath(String path) {
            path = path.trim();
            if (path.contains("['") || path.contains("[\"")) {
                return path.replaceAll("\\['([^']+)']\\.", "$1.")
                           .replaceAll("\\[\"([^\"]+)\"\\].", "$1.")
                           .replaceAll("\\['([^']+)']$", "$1")
                           .replaceAll("\\[\"([^\"]+)\"]$", "$1")
                           .split("\\.");
            }
            return path.split("\\.");
        }
    }
}
