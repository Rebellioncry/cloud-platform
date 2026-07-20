package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.entity.IotRuleExecLog;

public interface RuleLogService {

    PageResult<IotRuleExecLog> list(int page, int size, String ruleId, Integer status);

    void saveLog(IotRuleExecLog log);
}
