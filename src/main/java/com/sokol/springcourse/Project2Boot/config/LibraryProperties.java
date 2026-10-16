package com.sokol.springcourse.Project2Boot.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configures lending limits. */
@Validated
@ConfigurationProperties("library")
public record LibraryProperties(
        @Min(1) int loanPeriodDays,
        @Min(1) int defaultPageSize,
        @Min(1) int maxPageSize) {
}
