package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkMessage;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpDataSink implements DataSink {

    private DataSinkConfig config;
    private RestTemplate restTemplate;
    private boolean alive = false;

    private String url;
    private String method;
    private int timeout;
    private String contentType;
    private String authToken;

    @Override
    public String getType() {
        return "http";
    }

    @Override
    public void init(DataSinkConfig config) {
        this.config = config;
        this.url = config.getString("url", "http://localhost:8080");
        this.method = config.getString("method", "POST");
        this.timeout = config.getInt("timeout", 5000);
        this.contentType = config.getString("contentType", "application/json");
        this.authToken = config.getString("authToken", "");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.restTemplate = new RestTemplate(factory);
        this.alive = true;
        log.info("HTTP DataSink初始化: url={}, method={}, timeout={}ms", url, method, timeout);
    }

    @Override
    public void send(DataSinkMessage message) {
        if (!alive || restTemplate == null) {
            log.warn("HTTP DataSink未就绪, 消息丢弃: url={}", url);
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            if (!authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            if (message.getHeaders() != null) {
                message.getHeaders().forEach((k, v) -> headers.set(k, String.valueOf(v)));
            }

            String payload = message.getPayload() != null ? message.getPayload() : "";
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            HttpMethod httpMethod;
            switch (method.toUpperCase()) {
                case "GET": httpMethod = HttpMethod.GET; break;
                case "PUT": httpMethod = HttpMethod.PUT; break;
                case "DELETE": httpMethod = HttpMethod.DELETE; break;
                case "PATCH": httpMethod = HttpMethod.PATCH; break;
                default: httpMethod = HttpMethod.POST;
            }

            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
            log.debug("HTTP DataSink发送成功: {} {} -> status={}, body={}",
                    method, url, response.getStatusCodeValue(),
                    response.getBody() != null ? response.getBody().substring(0, Math.min(200, response.getBody().length())) : "");
        } catch (Exception e) {
            log.error("HTTP DataSink发送失败: {} {}", method, url, e);
        }
    }

    @Override
    public void close() {
        this.alive = false;
        this.restTemplate = null;
        log.info("HTTP DataSink已关闭: url={}", url);
    }

    @Override
    public boolean isAlive() {
        return alive;
    }
}
