package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.RuleDTO;
import org.lyz.iot.entity.IotRule;

public interface RuleService {

    PageResult<IotRule> list(int page, int size, String name);

    IotRule getById(String id);

    IotRule create(RuleDTO dto);

    void update(String id, RuleDTO dto);

    void delete(String id);

    void start(String id);

    void stop(String id);
}
