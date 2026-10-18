# Library architecture

## Current product

The application manages books and readers, assigns and returns books, searches titles, and lists stable pages. It is a loopback educational demonstration with fictional data, not an authenticated multi-user service. Course attribution remains in the README.

## Components

Spring MVC controllers serve Thymeleaf pages through `BookService` and `PeopleService`. Spring Data JPA repositories persist `Book` and `Person`. Spring Security retains CSRF checks while permitting all requests. Constructor injection supplies repositories, settings and the UTC clock.

Flyway owns schema creation and Hibernate validates it. The default demo profile uses an in-memory H2 database and synthetic seed data. The PostgreSQL profile uses explicit connection settings; Compose persists database data in the `library-data` volume.

## Lending invariants

- A book stores one current borrower and checkout time; both must be present together or absent together.
- Operations requiring both rows lock the reader before the book. Row locks serialize competing checkouts.
- Assigning a borrowed book to another reader fails; repeating the same assignment preserves the original checkout time.
- Metadata updates mutate the existing entity without replacing its lending state.
- A borrowed book and a reader with outstanding loans cannot be deleted.
- Returning a book clears its current checkout state. There is currently no separate loan-history entity.
- Overdue evaluation uses the configured duration and UTC clock. Page size, query length and offsets are bounded.

## Verification owners

`BookServiceTest` covers lending transitions and time boundaries. `BookServiceRegressionTest` covers preservation of checkout metadata. `PeopleServiceTest` covers reader operations. `LibraryWebTest` exercises rendered forms, field-binding restrictions, CSRF and response behavior. `PostgresLibraryIT` checks migrated storage, schema constraints and two real competing database writers through Testcontainers.

The PostgreSQL race test holds the book row, observes both writers waiting, and then requires one successful checkout and one conflict. H2 is not a substitute for that database-specific check.

## Build and runtime

The Maven project identity and Java package names remain unchanged. Production sources use `src/main/`; Maven explicitly reads checks from `tests/java/`. Docker builds the same project and runs the application as a non-root user. Public container context is restricted to build metadata, product sources and tests. Setup and operator commands belong to `../README.md`; development commands belong to `development.md`.
