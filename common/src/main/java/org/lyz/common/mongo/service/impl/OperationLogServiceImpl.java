package org.lyz.common.mongo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.mongo.entity.OperationLog;
import org.lyz.common.mongo.service.OperationLogService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final MongoTemplate mongoTemplate;

    @Async
    @Override
    public void save(OperationLog operationLog) {
        try {
            mongoTemplate.insert(operationLog);
        } catch (Exception e) {
            OperationLogServiceImpl.log.error("保存操作日志失败", e);
        }
    }
}
