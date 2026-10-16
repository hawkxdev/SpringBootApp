package com.sokol.springcourse.Project2Boot.services;

import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.repositories.BookRepository;
import com.sokol.springcourse.Project2Boot.repositories.PeopleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Exercises reader invariants. */
class PeopleServiceTest {
    private final PeopleRepository people = mock(PeopleRepository.class);
    private final BookRepository books = mock(BookRepository.class);
    private final PeopleService service = new PeopleService(people, books);

    @Test
    void readerWithLoansCannotBeDeleted() {
        Person reader = new Person(1, "Reader", "1980");
        when(people.findByIdForUpdate(1)).thenReturn(Optional.of(reader));
        when(books.existsByPersonId(1)).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> service.deleteById(1)).getStatusCode());
        verify(people, never()).delete(any(Person.class));
    }

    @Test
    void availableReaderCanBeDeleted() {
        Person reader = new Person(1, "Reader", "1980");
        when(people.findByIdForUpdate(1)).thenReturn(Optional.of(reader));
        service.deleteById(1);
        verify(people).delete(reader);
    }

    @Test
    void editUsesPathIdentityAndAllowsUnchangedName() {
        Person existing = new Person(1, "Reader", "1980");
        when(people.findByIdForUpdate(1)).thenReturn(Optional.of(existing));
        service.update(1, new Person(99, "Reader", "1981"));
        verify(people).existsByNameAndIdNot("Reader", 1);
        assertEquals(1, existing.getId());
        assertEquals("1981", existing.getYearOfBirth());
        verify(people).save(existing);
    }

    @Test
    void duplicateNameDoesNotOverwriteExistingReader() {
        Person existing = new Person(1, "Original", "1980");
        when(people.findByIdForUpdate(1)).thenReturn(Optional.of(existing));
        when(people.existsByNameAndIdNot("Taken", 1)).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> service.update(1, new Person(0, "Taken", "1981"))).getStatusCode());
        assertEquals("Original", existing.getName());
        verify(people, never()).save(any());
    }
}
