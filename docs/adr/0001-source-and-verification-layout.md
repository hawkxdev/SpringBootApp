# Source and verification layout

Status: accepted for the structural transition requested on 18.10.2026.

## Context

The library already has a reproducible Maven build, H2 demonstration, PostgreSQL Compose runtime and isolated integration tests. Its product behavior and deployment contract must remain stable while project instructions and knowledge become explicit.

## Decision

Keep production code and resources under `src/main/`. Place automated Java checks under `tests/java/` and declare that path in Maven. Keep project scripts, accepted feature specifications and architecture documents in their dedicated root roles. Keep the existing artifact identity, Java packages, configuration names, ports, schema and volume identity.

The container build copies the new test directory before Maven verification. The local verification script checks the JDK prerequisite, resolves the repository root and runs the full PostgreSQL profile. Private context is outside the public tree and build context.

## Consequences

Existing user run commands remain valid. Both host and container builds need the test-path adaptation. Structural acceptance must include the fast suite, the real PostgreSQL suite, a container build and a synthetic runtime check. This decision adds no feature scope or public-delivery authority.
