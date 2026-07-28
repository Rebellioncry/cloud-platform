package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.context.TenantHelper;
import org.lyz.common.core.context.UserContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.RuleDTO;
import org.lyz.iot.entity.IotRule;
import org.lyz.iot.dao.IotRuleDao;
import org.lyz.iot.service.RuleService;
import org.lyz.iot.rule.engine.RuleEngineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final IotRuleDao ruleDao;
    private final RuleEngineService ruleEngineService;

    @Override
    public PageResult<IotRule> list(int page, int size, String name) {
        Page<IotRule> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotRule> wrapper = new LambdaQueryWrapper<>();
        if (!UserContext.isPlatformAdmin()) {
            String tenantId = UserContext.getTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                wrapper.eq(IotRule::getTenantId, tenantId);
            }
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(IotRule::getName, name);
        }
        wrapper.orderByDesc(IotRule::getCreateTime);
        IPage<IotRule>[] resultRef = new IPage[1];
        TenantHelper.ignore(() -> resultRef[0] = ruleDao.page(pageParam, wrapper));
        return PageResult.of(resultRef[0].getTotal(), page, size, resultRef[0].getRecords());
    }

    @Override
    public IotRule getById(String id) {
        IotRule[] ref = new IotRule[1];
        TenantHelper.ignore(() -> ref[0] = ruleDao.getById(id));
        if (ref[0] == null) {
            throw new BusinessException("规则不存在");
        }
        return ref[0];
    }

    @Override
    @Transactional
    public IotRule create(RuleDTO dto) {
        IotRule rule = new IotRule();
        String tenantId = UserContext.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            rule.setTenantId(tenantId);
        }
        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setRuleType(dto.getRuleType() != null ? dto.getRuleType() : 0);
        rule.setRuleModel(dto.getRuleModel());
        rule.setStatus(0);
        rule.setMatchCount(0L);
        ruleDao.save(rule);
        return rule;
    }

    @Override
    @Transactional
    public void update(String id, RuleDTO dto) {
        IotRule rule = getById(id);
        LambdaUpdateWrapper<IotRule> update = new LambdaUpdateWrapper<>();
        update.eq(IotRule::getId, id);
        if (dto.getName() != null) update.set(IotRule::getName, dto.getName());
        if (dto.getDescription() != null) update.set(IotRule::getDescription, dto.getDescription());
        if (dto.getRuleType() != null) update.set(IotRule::getRuleType, dto.getRuleType());
        if (dto.getRuleModel() != null) update.set(IotRule::getRuleModel, dto.getRuleModel());
        if (dto.getStatus() != null) update.set(IotRule::getStatus, dto.getStatus());
        ruleDao.update(null, update);

        if (dto.getRuleModel() != null && ruleEngineService.isRuleRunning(id)) {
            ruleEngineService.unloadRule(id);
            IotRule updatedRule = getById(id);
            ruleEngineService.loadRule(updatedRule);
            log.info("运行中规则已热重载: {}", id);
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        IotRule rule = getById(id);
        ruleEngineService.unloadRule(id);
        ruleDao.removeById(id);
    }

    @Override
    @Transactional
    public void start(String id) {
        IotRule rule = getById(id);
        if (rule.getStatus() == 1) {
            throw new BusinessException("规则已在运行中");
        }
        LambdaUpdateWrapper<IotRule> update = new LambdaUpdateWrapper<>();
        update.eq(IotRule::getId, id).set(IotRule::getStatus, 1);
        ruleDao.update(null, update);
        ruleEngineService.loadRule(rule);
        log.info("规则已启动: {} - {}", rule.getId(), rule.getName());
    }

    @Override
    @Transactional
    public void stop(String id) {
        IotRule rule = getById(id);
        if (rule.getStatus() == 0) {
            throw new BusinessException("规则已停止");
        }
        LambdaUpdateWrapper<IotRule> update = new LambdaUpdateWrapper<>();
        update.eq(IotRule::getId, id).set(IotRule::getStatus, 0);
        ruleDao.update(null, update);
        ruleEngineService.unloadRule(id);
        log.info("规则已停止: {} - {}", rule.getId(), rule.getName());
    }
}
