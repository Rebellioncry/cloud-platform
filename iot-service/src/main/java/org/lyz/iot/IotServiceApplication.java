package org.lyz.iot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "org.lyz")
@MapperScan(basePackages = "org.lyz.iot.mapper.mysql")
public class IotServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IotServiceApplication.class, args);
    }
}
