package org.lyz.iot.rule.engine;

import org.lyz.iot.rule.model.RuleData;
import org.lyz.iot.rule.model.RuleModel;
import org.lyz.iot.rule.model.RuleNodeModel;

import java.util.concurrent.CompletableFuture;

public interface TaskExecutor {

    String getType();

    void init(RuleNodeModel nodeModel);

    CompletableFuture<RuleData> execute(RuleData ruleData);

    default void stop() {}
}
