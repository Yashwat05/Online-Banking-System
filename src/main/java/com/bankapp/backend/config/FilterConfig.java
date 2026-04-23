package com.bankapp.backend.config;

import com.bankapp.backend.security.AuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilter() {

        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new AuthFilter());
        registration.addUrlPatterns("/*"); // apply to all endpoints

        return registration;
    }
}