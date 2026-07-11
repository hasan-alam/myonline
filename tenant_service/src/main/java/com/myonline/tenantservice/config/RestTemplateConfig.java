package com.myonline.tenantservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for {@link RestTemplate} used by {@link com.myonline.tenantservice.client.AuthServiceClient}
 * to communicate with the Auth Microservice.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);   // 5 seconds
        factory.setReadTimeout(10_000);     // 10 seconds
        return new RestTemplate(factory);
    }
}
