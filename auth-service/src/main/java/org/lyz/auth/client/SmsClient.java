package org.lyz.auth.client;

import org.lyz.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "sms-service")
public interface SmsClient {

    @PostMapping("/send")
    Result<Void> send(@RequestBody Map<String, Object> request);
}
