package com.sokol.springcourse.Project2Boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Protects local forms. */
@Configuration
public class SecurityConfiguration {
    /** Preserves CSRF protection. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll()).build();
    }
}
