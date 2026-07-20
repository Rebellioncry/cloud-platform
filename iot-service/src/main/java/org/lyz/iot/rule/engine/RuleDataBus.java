package org.lyz.iot.rule.engine;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.model.RuleData;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
@Component
public class RuleDataBus implements ApplicationListener<RuleDataBus.RuleEvent> {

    private final List<Consumer<RuleData>> listeners = new CopyOnWriteArrayList<>();

    public void publish(RuleData ruleData) {
        log.debug("RuleDataBus发布事件: ruleId={}, keys={}", ruleData.getRuleId(), ruleData.getAll().keySet());
        for (Consumer<RuleData> listener : listeners) {
            try {
                listener.accept(ruleData);
            } catch (Exception e) {
                log.error("RuleDataBus监听器处理异常", e);
            }
        }
    }

    public void subscribe(Consumer<RuleData> listener) {
        listeners.add(listener);
        log.debug("RuleDataBus新增监听器, 当前监听器数: {}", listeners.size());
    }

    public void unsubscribe(Consumer<RuleData> listener) {
        listeners.remove(listener);
    }

    @Override
    public void onApplicationEvent(RuleEvent event) {
        publish(event.getRuleData());
    }

    public static class RuleEvent extends ApplicationEvent {
        private final RuleData ruleData;

        public RuleEvent(Object source, RuleData ruleData) {
            super(source);
            this.ruleData = ruleData;
        }

        public RuleData getRuleData() {
            return ruleData;
        }
    }
}
