package com.sokol.springcourse.Project2Boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/** Starts the library. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class Project2BootApplication {
    /** Launches the application. */
    public static void main(String[] args) {
        SpringApplication.run(Project2BootApplication.class, args);
    }
}
