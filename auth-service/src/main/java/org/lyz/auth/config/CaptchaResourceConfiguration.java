package org.lyz.auth.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaptchaResourceConfiguration {

    @Bean
    public ResourceStore resourceStore() {
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();

        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "META-INF/cut-image/resource/1.jpg", "default"));

        return resourceStore;
    }
}
