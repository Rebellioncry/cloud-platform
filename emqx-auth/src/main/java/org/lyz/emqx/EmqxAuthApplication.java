package org.lyz.emqx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan(basePackages = "org.lyz.emqx.mapper")
public class EmqxAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmqxAuthApplication.class, args);
    }
}
