package com.sokol.springcourse.Project2Boot;

import com.sokol.springcourse.Project2Boot.models.Book;
import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.repositories.BookRepository;
import com.sokol.springcourse.Project2Boot.repositories.PeopleRepository;
import com.sokol.springcourse.Project2Boot.services.BookService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/** Checks PostgreSQL behavior. */
@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers
class PostgresLibraryIT {
    @Container
    static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired private BookRepository books;
    @Autowired private PeopleRepository people;
    @Autowired private BookService lending;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;
    @Autowired private PlatformTransactionManager transactions;

    /** Configures isolated database. */
    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
    }

    /** Clears scenario data. */
    @BeforeEach
    void clearData() {
        books.deleteAllInBatch();
        people.deleteAllInBatch();
    }

    @Test
    void migratedSchemaSupportsCheckoutAndReturn() {
        Book book = book();
        Person reader = reader("Alex Reader");
        lending.assign(book.getId(), reader.getId());
        Book borrowed = lending.getBookById(book.getId());
        assertEquals("Alex Reader", borrowed.getPerson().getName());
        assertNotNull(borrowed.getBookingTime());
        lending.release(book.getId());
        Book returned = lending.getBookById(book.getId());
        assertNull(returned.getPerson());
        assertNull(returned.getBookingTime());
        assertEquals("17", jdbc.queryForObject("select split_part(version(), ' ', 2)", String.class).split("\\.")[0]);
    }

    @Test
    void databaseRejectsPartialCheckoutState() {
        Book book = book();
        Person reader = reader("Alex Reader");
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("update book set person_id = ? where id = ?", reader.getId(), book.getId()));
        assertNull(lending.getBookById(book.getId()).getPerson());
    }

    @Test
    @Timeout(45)
    void concurrentReadersCannotBothCheckOutTheSameBook() throws Exception {
        Book book = book();
        Person first = reader("First Reader");
        Person second = reader("Second Reader");
        List<Future<Integer>> attempts = new ArrayList<>();
        try (var executor = Executors.newFixedThreadPool(2)) {
            new TransactionTemplate(transactions).executeWithoutResult(transaction -> {
                jdbc.queryForObject("select id from book where id = ? for update", Integer.class, book.getId());
                attempts.add(executor.submit(() -> checkoutStatus(book.getId(), first.getId())));
                attempts.add(executor.submit(() -> checkoutStatus(book.getId(), second.getId())));
                await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertEquals(2, blockedBookWriters()));
            });
            List<Integer> statuses = List.of(attempts.get(0).get(15, TimeUnit.SECONDS),
                    attempts.get(1).get(15, TimeUnit.SECONDS)).stream().sorted().toList();
            assertEquals(List.of(200, 409), statuses);
        }
        Book borrowed = lending.getBookById(book.getId());
        assertTrue(Set.of(first.getId(), second.getId()).contains(borrowed.getPerson().getId()));
        assertNotNull(borrowed.getBookingTime());
    }

    /** Counts blocked writers. */
    private int blockedBookWriters() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("select count(*) from pg_stat_activity "
                     + "where datname = current_database() and wait_event_type = 'Lock' and query like '%book%'")) {
            result.next();
            return result.getInt(1);
        }
    }

    /** Captures checkout outcomes. */
    private int checkoutStatus(int bookId, int readerId) {
        try {
            lending.assign(bookId, readerId);
            return 200;
        } catch (ResponseStatusException exception) {
            return exception.getStatusCode().value();
        }
    }

    /** Creates a fixture. */
    private Book book() { return books.saveAndFlush(new Book(0, "Shared book", "Author", "2000")); }

    /** Creates a reader. */
    private Person reader(String name) { return people.saveAndFlush(new Person(0, name, "1980")); }
}
