package org.lyz.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.common.mongo.entity.OperationLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "审计日志")
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final MongoTemplate mongoTemplate;

    @Operation(summary = "审计日志列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "httpMethod", required = false) String httpMethod,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {

        List<Criteria> criteriaList = new ArrayList<>();

        if (username != null && !username.isEmpty()) {
            criteriaList.add(Criteria.where("username").regex(username, "i"));
        }
        if (module != null && !module.isEmpty()) {
            criteriaList.add(Criteria.where("module").is(module));
        }
        if (httpMethod != null && !httpMethod.isEmpty()) {
            criteriaList.add(Criteria.where("httpMethod").is(httpMethod.toUpperCase()));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        if (url != null && !url.isEmpty()) {
            criteriaList.add(Criteria.where("url").regex(url, "i"));
        }
        if (startTime != null && !startTime.isEmpty()) {
            criteriaList.add(Criteria.where("createTime").gte(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            criteriaList.add(Criteria.where("createTime").lte(endTime));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, OperationLog.class);
        query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        query.skip((long) (page - 1) * size).limit(size);
        List<OperationLog> list = mongoTemplate.find(query, OperationLog.class);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }
}
