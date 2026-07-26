package org.lyz.auth.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class CaptchaResourceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CaptchaResourceConfiguration.class);

    @Bean
    public ResourceStore resourceStore(ApplicationContext ctx) throws IOException {
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();

        org.springframework.core.io.Resource[] resources = ctx.getResources("classpath*:META-INF/cut-image/resource/*");
        int count = 0;
        for (org.springframework.core.io.Resource res : resources) {
            String filename = res.getFilename();
            if (filename != null && (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))) {
                resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "META-INF/cut-image/resource/" + filename, "default"));
                log.info("加载验证码背景图: {}", filename);
                count++;
            }
        }

        log.info("共加载 {} 张验证码背景图", count);
        return resourceStore;
    }
}
