package org.lyz.iot.rule.engine.executor.transform;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
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
public class ConditionNode implements TaskExecutorProvider {

    private static final AviatorEvaluatorInstance AVIATOR = AviatorEvaluator.getInstance();

    @Override
    public String getType() {
        return "condition";
    }

    @Override
    public TaskExecutor createExecutor(RuleNodeModel nodeModel) {
        return new Executor(nodeModel);
    }

    private static class Executor implements TaskExecutor {
        private final RuleNodeModel nodeModel;
        private String expression;

        public Executor(RuleNodeModel nodeModel) {
            this.nodeModel = nodeModel;
            this.expression = nodeModel.getStringProperty("expression", "true");
        }

        @Override
        public String getType() {
            return "condition";
        }

        @Override
        public void init(RuleNodeModel nodeModel) {
        }

        @Override
        public CompletableFuture<RuleData> execute(RuleData ruleData) {
            try {
                Map<String, Object> env = new HashMap<>(ruleData.getAll());
                Object result = AVIATOR.execute(expression, env);

                boolean passed;
                if (result instanceof AviatorBoolean) {
                    passed = ((AviatorBoolean) result).booleanValue(env);
                } else if (result instanceof Boolean) {
                    passed = (Boolean) result;
                } else {
                    passed = result != null;
                }

                if (!passed) {
                    ruleData.setStopped(true);
                    log.info("条件节点不满足, 停止执行: expression={}, result={}", expression, result);
                } else {
                    log.info("条件节点满足, 继续执行: expression={}, result={}", expression, result);
                }

                return CompletableFuture.completedFuture(ruleData);
            } catch (Exception e) {
                log.error("条件节点执行失败: expression={}", expression, e);
                ruleData.setStopped(true);
                return CompletableFuture.completedFuture(ruleData);
            }
        }
    }
}
