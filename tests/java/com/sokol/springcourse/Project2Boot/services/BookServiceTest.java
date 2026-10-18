package com.sokol.springcourse.Project2Boot.services;

import com.sokol.springcourse.Project2Boot.config.LibraryProperties;
import com.sokol.springcourse.Project2Boot.models.Book;
import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.repositories.BookRepository;
import com.sokol.springcourse.Project2Boot.repositories.PeopleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Exercises lending transitions. */
class BookServiceTest {
    private final BookRepository books = mock(BookRepository.class);
    private final PeopleRepository people = mock(PeopleRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC);
    private BookService service;

    /** Creates isolated services. */
    @BeforeEach
    void setUp() { service = new BookService(books, people, clock, new LibraryProperties(10, 10, 100)); }

    @Test
    void checkoutRecordsBorrowerAndUtcTime() {
        Book book = new Book(1, "Title", "Author", "2000");
        Person reader = new Person(2, "Reader", "1980");
        when(books.findByIdForUpdate(1)).thenReturn(Optional.of(book));
        when(people.findByIdForUpdate(2)).thenReturn(Optional.of(reader));
        service.assign(1, 2);
        assertSame(reader, book.getPerson());
        assertEquals(LocalDateTime.now(clock), book.getBookingTime());
        verify(books).save(book);
    }

    @Test
    void anotherReaderCannotOverwriteExistingLoan() {
        Book book = new Book(1, "Title", "Author", "2000");
        Person original = new Person(3, "First", "1980");
        LocalDateTime checkout = LocalDateTime.now(clock).minusDays(2);
        book.setPerson(original);
        book.setBookingTime(checkout);
        when(books.findByIdForUpdate(1)).thenReturn(Optional.of(book));
        when(people.findByIdForUpdate(2)).thenReturn(Optional.of(new Person(2, "Other", "1980")));
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.assign(1, 2));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertSame(original, book.getPerson());
        assertEquals(checkout, book.getBookingTime());
        verify(books, never()).save(any());
    }

    @Test
    void repeatedCheckoutDoesNotExtendLoanPeriod() {
        Person reader = new Person(2, "Reader", "1980");
        Book book = new Book(1, "Title", "Author", "2000");
        book.setPerson(reader);
        LocalDateTime checkout = LocalDateTime.now(clock).minusDays(5);
        book.setBookingTime(checkout);
        when(books.findByIdForUpdate(1)).thenReturn(Optional.of(book));
        when(people.findByIdForUpdate(2)).thenReturn(Optional.of(reader));
        service.assign(1, 2);
        assertEquals(checkout, book.getBookingTime());
        verify(books, never()).save(any());
    }

    @Test
    void returnClearsBothPartsOfCheckoutState() {
        Book book = new Book(1, "Title", "Author", "2000");
        book.setPerson(new Person(2, "Reader", "1980"));
        book.setBookingTime(LocalDateTime.now(clock));
        when(books.findByIdForUpdate(1)).thenReturn(Optional.of(book));
        service.release(1);
        assertNull(book.getPerson());
        assertNull(book.getBookingTime());
        verify(books).save(book);
    }

    @Test
    void missingReaderDoesNotModifyBook() {
        when(people.findByIdForUpdate(2)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.assign(1, 2));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(books);
    }

    @Test
    void missingBookReturnsNotFoundInsteadOfOptionalFailure() {
        when(books.findByIdForUpdate(5)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> service.release(5)).getStatusCode());
        verify(books, never()).save(any());
    }

    @Test
    void loanCannotBeDeleted() {
        Book book = new Book(1, "Title", "Author", "2000");
        book.setPerson(new Person(2, "Reader", "1980"));
        when(books.findByIdForUpdate(1)).thenReturn(Optional.of(book));
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> service.deleteBook(1)).getStatusCode());
        verify(books, never()).delete(any(Book.class));
    }

    @Test
    void overdueStartsStrictlyAfterConfiguredDuration() {
        Book book = new Book(1, "Title", "Author", "2000");
        when(books.findById(1)).thenReturn(Optional.of(book));
        book.setBookingTime(LocalDateTime.now(clock).minusDays(10));
        assertFalse(service.getBookById(1).isOverdue());
        book.setBookingTime(book.getBookingTime().minusNanos(1));
        assertTrue(service.getBookById(1).isOverdue());
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,101", "2147483647,100"})
    void invalidPagingReturnsBadRequest(int page, int size) {
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.getBooksByPage(page, size, false)).getStatusCode());
        verifyNoInteractions(books);
    }

    @Test
    void blankSearchDoesNotRunAnUnboundedQuery() {
        assertTrue(service.search("  ").isEmpty());
        verifyNoInteractions(books);
    }
}
