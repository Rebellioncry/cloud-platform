package org.lyz.iot.rule.engine;

import org.lyz.iot.rule.model.RuleNodeModel;

public interface TaskExecutorProvider {

    String getType();

    TaskExecutor createExecutor(RuleNodeModel nodeModel);

    default boolean supports(String type) {
        return getType().equals(type);
    }
}
