package com.sokol.springcourse.Project2Boot.services;

import com.sokol.springcourse.Project2Boot.config.LibraryProperties;
import com.sokol.springcourse.Project2Boot.models.Book;
import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.repositories.BookRepository;
import com.sokol.springcourse.Project2Boot.repositories.PeopleRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Coordinates book lending. */
@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository books;
    private final PeopleRepository people;
    private final Clock clock;
    private final LibraryProperties properties;

    /** Receives lending dependencies. */
    public BookService(BookRepository books, PeopleRepository people, Clock clock, LibraryProperties properties) {
        this.books = books;
        this.people = people;
        this.clock = clock;
        this.properties = properties;
    }

    /** Lists reader loans. */
    public List<Book> getBooksByPerson(Person person) {
        return books.findByPersonIdOrderByTitleAsc(person.getId()).stream().map(this::prepare).toList();
    }

    /** Finds book details. */
    public Book getBookById(int id) {
        return prepare(books.findById(id).orElseThrow(() -> missingBook()));
    }

    /** Creates book details. */
    @Transactional
    public Book saveBook(Book form) {
        return books.save(new Book(0, form.getTitle(), form.getAuthor(), form.getYear()));
    }

    /** Preserves checkout metadata. */
    @Transactional
    public void updateBook(int id, Book form) {
        Book book = lockedBook(id);
        book.setTitle(form.getTitle());
        book.setAuthor(form.getAuthor());
        book.setYear(form.getYear());
        books.save(book);
    }

    /** Assigns available books. */
    @Transactional
    public void assign(int bookId, int personId) {
        Person person = people.findByIdForUpdate(personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Reader not found"));
        Book book = lockedBook(bookId);
        if (book.getPerson() != null) {
            if (book.getPerson().getId() == personId) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Return this book before assigning another reader");
        }
        book.setPerson(person);
        book.setBookingTime(LocalDateTime.now(clock));
        books.save(book);
    }

    /** Returns borrowed books. */
    @Transactional
    public void release(int id) {
        Book book = lockedBook(id);
        book.setPerson(null);
        book.setBookingTime(null);
        books.save(book);
    }

    /** Deletes available books. */
    @Transactional
    public void deleteBook(int id) {
        Book book = lockedBook(id);
        if (book.getPerson() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Return this book before deleting it");
        }
        books.delete(book);
    }

    /** Lists stable pages. */
    public Page<Book> getBooksByPage(int page, int size, Boolean sortByYear) {
        if (page < 0 || size < 1 || size > properties.maxPageSize()
                || (long) page * size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or page size");
        }
        Sort sort = Sort.by(Boolean.TRUE.equals(sortByYear) ? "year" : "title").and(Sort.by("id"));
        return books.findAll(PageRequest.of(page, size, sort)).map(this::prepare);
    }

    /** Searches book titles. */
    public List<Book> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        if (query.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query is too long");
        }
        return books.findByTitleContainingIgnoreCase(query.strip(),
                PageRequest.of(0, properties.maxPageSize(), Sort.by("title").and(Sort.by("id"))))
                .stream().map(this::prepare).toList();
    }

    /** Locks existing books. */
    private Book lockedBook(int id) {
        return books.findByIdForUpdate(id).orElseThrow(() -> missingBook());
    }

    /** Describes missing books. */
    private static ResponseStatusException missingBook() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
    }

    /** Evaluates overdue status. */
    private Book prepare(Book book) {
        book.setOverdue(book.getBookingTime() != null && book.getBookingTime()
                .isBefore(LocalDateTime.now(clock).minusDays(properties.loanPeriodDays())));
        return book;
    }
}
