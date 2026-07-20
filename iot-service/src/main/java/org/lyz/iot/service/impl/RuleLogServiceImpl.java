package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.entity.IotRuleExecLog;
import org.lyz.iot.dao.IotRuleExecLogDao;
import org.lyz.iot.service.RuleLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleLogServiceImpl implements RuleLogService {

    private final IotRuleExecLogDao execLogDao;

    @Override
    public PageResult<IotRuleExecLog> list(int page, int size, String ruleId, Integer status) {
        Page<IotRuleExecLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotRuleExecLog> wrapper = new LambdaQueryWrapper<>();
        if (ruleId != null && !ruleId.isEmpty()) {
            wrapper.eq(IotRuleExecLog::getRuleId, ruleId);
        }
        if (status != null) {
            wrapper.eq(IotRuleExecLog::getStatus, status);
        }
        wrapper.orderByDesc(IotRuleExecLog::getExecuteTime);
        IPage<IotRuleExecLog> result = execLogDao.pageIgnoreTenant(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    @Async
    public void saveLog(IotRuleExecLog execLog) {
        try {
            execLogDao.saveIgnoreTenant(execLog);
        } catch (Exception e) {
            log.error("保存规则执行日志失败", e);
        }
    }
}
