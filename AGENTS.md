# Library project instructions

## Entry and ownership

Read `.agents/skills/project-guide/SKILL.md` and the relevant project documents before project work. Use the current issue or explicitly assigned task as the scope. Read the relevant specification under `specs/` when one exists; an unassigned idea is not implementation authority.

`README.md` owns setup and user operation. `docs/architecture.md` owns current product structure and invariants. `docs/development.md` owns development and verification commands. Accepted technical decisions live in `docs/adr/`.

## Product and structure

This is a local educational library lending application. It currently provides books, readers, checkout, return, search and pagination. It has no user accounts or authorization; Spring Security preserves CSRF protection for forms. Keep the documented loopback demonstration boundary unless a task explicitly changes it.

Java 21 is the compilation target. Spring Boot 3.5, MVC, Thymeleaf, JPA/Hibernate, Flyway and PostgreSQL are declared in `pom.xml`; H2 supports the default demo profile. Preserve dependency and package identities during structural work.

- `src/main/java/`: production Java sources.
- `src/main/resources/`: templates, configuration, migrations and synthetic demo data.
- `tests/java/`: unit, MVC and PostgreSQL integration checks.
- `scripts/`: local project verification entrypoints.
- `specs/`: accepted feature specifications when assigned.
- `docs/`: public project knowledge and architecture decisions.

## Implementation constraints

Preserve the reader-before-book lock order for operations that acquire both rows. Editing book metadata must preserve borrower and checkout time. Repeated checkout to the same reader must not reset that time. Returning a book clears both checkout fields; the database constraint requires them to be either both present or both absent.

Do not delete a borrowed book or a reader with outstanding loans. Keep UTC loan calculations and configurable lending/page limits. Preserve bounded stable pagination and case-insensitive bounded search. Bind only editable form fields, preserve CSRF checks, and do not expose database errors or stack traces.

Use the existing constructor injection, service/repository boundaries and project validation style. A dependency upgrade or a new lending policy needs its own task and checks. Do not describe proposed reservations, roles or messaging as existing features.

## Verification and execution

Run commands from the repository root unless `docs/development.md` says otherwise. `./mvnw clean verify` runs the fast suite. `./scripts/check.sh` also runs the real PostgreSQL integration suite and requires Docker. JDK 21 or newer must be selected; `JAVA_HOME` controls Maven when the shell default is older.

Use isolated synthetic data and test containers. Preserve `.env`, running demonstration containers and persistent database volumes. Do not run destructive cleanup or replace a running demo as an incidental check. Run tests, quality checks and builds locally; do not launch or restore GitHub Actions execution.

## Git and public content

Keep one Git root. Preserve unrelated changes and existing branches. Obtain the repository owner's approval of the proposed changes and commit message before every commit. Default-branch delivery uses a pull request; the repository owner performs the final merge. Publication, visibility changes and deployment require their actual task authority. Never force push or delete a public branch.

Keep credentials, local environment files, private working notes and machine-specific access metadata outside Git and container build contexts. Public project guidance must work using only files delivered in this repository.

## Knowledge maintenance

Update the existing owner when behavior, commands or assumptions change. Keep evidence limits explicit and remove conflicting active instructions. Add a focused project skill only for useful reusable project knowledge; ordinary documentation may be sufficient. Keep project-guide focused on public project knowledge and navigation.
