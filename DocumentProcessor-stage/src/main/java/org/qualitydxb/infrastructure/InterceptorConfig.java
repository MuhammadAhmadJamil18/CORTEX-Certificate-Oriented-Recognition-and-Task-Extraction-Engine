package org.qualitydxb.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorConfig {
    @Bean
    public ApiInterceptor apiInterceptor() {
        return new ApiInterceptor();
    }
}


