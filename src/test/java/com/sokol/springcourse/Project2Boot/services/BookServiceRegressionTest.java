package com.sokol.springcourse.Project2Boot.services;

import com.sokol.springcourse.Project2Boot.models.Book;
import com.sokol.springcourse.Project2Boot.config.LibraryProperties;
import com.sokol.springcourse.Project2Boot.repositories.PeopleRepository;
import java.time.Clock;
import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.repositories.BookRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceRegressionTest {

    @Test
    void editingMetadataPreservesCheckoutTimestampAndBorrower() {
        BookRepository repository = mock(BookRepository.class);
        Book existing = new Book(1, "Original title", "Author", "2000");
        Person reader = new Person(1, "Alex Reader", "1980");
        LocalDateTime checkout = LocalDateTime.of(2023, 1, 1, 12, 0);
        existing.setPerson(reader);
        existing.setBookingTime(checkout);
        when(repository.findByIdForUpdate(1)).thenReturn(Optional.of(existing));

        new BookService(repository, mock(PeopleRepository.class), Clock.systemUTC(),
                new LibraryProperties(10, 10, 100)).updateBook(1, new Book(0, "Edited title", "Author", "2001"));

        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(repository).save(saved.capture());
        assertEquals(checkout, saved.getValue().getBookingTime());
        assertEquals(reader, saved.getValue().getPerson());
        assertEquals("Edited title", saved.getValue().getTitle());
    }
}
