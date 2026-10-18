# Development and verification

## Prerequisites

Use JDK 21 or newer, Git and the repository Maven Wrapper. Docker is required for PostgreSQL integration checks and the Compose demonstration. The wrapper downloads its distribution on first use; offline mode requires the corresponding dependencies to be cached.

Select a compatible JDK explicitly when the shell default is older. Set `JAVA_HOME` to the actual installed JDK directory. The project does not change the machine-wide Java selection. `scripts/check.sh` fails clearly when the selected JDK is too old.

## Commands

From the repository root:

```sh
./mvnw clean verify
./scripts/check.sh
./mvnw spring-boot:run
```

The first command runs unit and MVC checks. The second also enables `postgres-it` and runs the Testcontainers PostgreSQL checks. The third starts the synthetic H2 demonstration on loopback. `./scripts/check.sh -o` uses cached Maven dependencies; it still requires a running Docker engine and the PostgreSQL test image.

The verification script resolves its own project root, so it may be invoked by absolute path from another directory. No activated Python or Node environment is needed. Maven still requires the documented JDK prerequisite.

## Test layout

Java test packages live under `tests/java/`; `pom.xml` declares this directory explicitly. Keep package names aligned with the existing application. Production resources and Flyway migrations remain under `src/main/resources/`.

Fast results are written under `target/surefire-reports/`; PostgreSQL integration results are under `target/failsafe-reports/`. Read the actual command exit code and complete test totals. A missing integration run must not be described as a passing database suite.

Use unit checks for isolated transitions and validation. Use MVC checks for form binding, escaping, HTTP status and CSRF. Use the real PostgreSQL suite for migrations, constraints and lock behavior. Preserve controlled clocks and the competing-writer test when changing lending behavior.

## Runtime and containers

The README owns full H2 and Compose instructions. `docker compose up --build -d` uses the repository root as its build context. Docker copies both `src/` and `tests/`, so its Maven build includes fast checks. Full PostgreSQL tests are run separately through `scripts/check.sh`.

Keep all private context and environment values outside the container context. Preserve existing demonstration containers and volumes during development. Rehearse images under a separate name with synthetic data; normal Compose shutdown preserves the volume, while volume deletion destroys local records.

## Contribution scope

Use a bounded task with observable acceptance. Feature specifications belong in `../specs/`; accepted architecture decisions belong in `adr/`. Local checks and a reviewed public payload precede a requested pull request. The repository owner performs the final merge. Do not add hosted execution as an inferred quality-check requirement.
