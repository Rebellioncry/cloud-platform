package org.lyz.iot.rule.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotRule;
import org.lyz.iot.dao.IotRuleDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngineInitializer implements CommandLineRunner {

    private final RuleEngineService ruleEngineService;
    private final IotRuleDao ruleDao;

    @Override
    public void run(String... args) {
        try {
            List<IotRule> enabledRules = ruleDao.listIgnoreTenant();
            int loaded = 0;
            for (IotRule rule : enabledRules) {
                if (rule.getStatus() == 1 && rule.getRuleModel() != null && !rule.getRuleModel().isBlank()) {
                    ruleEngineService.loadRule(rule);
                    loaded++;
                }
            }
            ruleEngineService.start();
            log.info("规则引擎初始化完成: 加载{}条规则", loaded);
        } catch (Exception e) {
            log.error("规则引擎初始化失败", e);
        }
    }
}
