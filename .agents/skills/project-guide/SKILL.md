---
name: project-guide
description: >
  WHEN: Library or SpringBootApp structure, setup, conventions and checks,
  устройство библиотеки, объясни или объясняй проект, проверь или проверяй
  команды разработки. WHEN NOT: internal project coordination, unrelated projects or unassigned
  product expansion.
allowed-tools: [Read, Grep, Glob, Bash]
---

# Project Guide

## Role

Explain the library lending application using the public repository. The assigned task and AGENTS.md determine the permitted work.

## Degrees of Freedom: MEDIUM

## Project knowledge

All paths are relative to the repository root.

| Need | Owner | When to read |
|---|---|---|
| Working boundaries and conventions | AGENTS.md | Before project work |
| Setup, operation and limitations | README.md | Before running or explaining the application |
| Components and lending invariants | docs/architecture.md | Before changing product behavior |
| Prerequisites and verification | docs/development.md | Before selecting or running checks |
| Accepted technical decisions | docs/adr/ | When a change affects an existing decision |
| Feature requirements | The assigned specification under specs/ | When a specification owns the task |
| Dependency versions and test configuration | pom.xml | Before build or compatibility changes |

Read the relevant owner before answering or acting. For a bounded question, read only the necessary context. Identify unavailable sources and unperformed checks instead of inventing results.

## Components and entry points

Production Java code lives in src/main/java/. Spring MVC controllers serve the /books and /people pages through services and JPA repositories. Templates, configuration and Flyway migrations live in src/main/resources/. The default demo profile uses synthetic H2 data; the PostgreSQL profile provides persistent storage.

Java 21 is the compilation target. pom.xml owns the exact Spring Boot and dependency versions. Preserve the existing service boundaries, constructor injection and validation conventions.

For lending changes, inspect the affected service, repository, model and schema together. Preserve the reader-before-book lock order, paired borrower and checkout time, and preservation of loan metadata when editing a book.

## Commands and verification

Run commands from the repository root unless docs/development.md says otherwise.

- ./mvnw clean verify runs the fast unit and MVC suite.
- ./scripts/check.sh also enables the PostgreSQL integration profile and requires Docker.
- ./mvnw spring-boot:run starts the default local demonstration.

Select JDK 21 or newer. Use isolated synthetic data and preserve existing demonstration containers and persistent volumes.

Checks live in tests/java/. BookServiceTest covers lending transitions and time boundaries; BookServiceRegressionTest covers metadata preservation; PeopleServiceTest covers reader operations. LibraryWebTest covers forms, binding, escaping, CSRF and responses. PostgresLibraryIT covers schema constraints and competing database writers.

H2 results do not establish PostgreSQL locking behavior. A successful build does not establish runtime or deployment acceptance. Report the checks actually performed and their limits.

## Knowledge upkeep

Correct affected public guidance at its existing project owner within task authority. Keep this guide independently usable from the public repository. Report unresolved knowledge gaps to the project lead.

## Harness enhancements

Use available tools or direct file reads. State a missing capability rather than assuming access to a particular local environment.
