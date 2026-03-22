package org.lyz.common.mongo.service;

import org.lyz.common.mongo.entity.OperationLog;

public interface OperationLogService {
    void save(OperationLog log);
}
