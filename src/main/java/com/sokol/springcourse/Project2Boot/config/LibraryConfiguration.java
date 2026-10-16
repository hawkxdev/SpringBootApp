package com.sokol.springcourse.Project2Boot.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Provides library dependencies. */
@Configuration
@EnableConfigurationProperties(LibraryProperties.class)
public class LibraryConfiguration implements WebMvcConfigurer {
    /** Opens the catalog. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/books");
    }

    /** Provides UTC time. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
