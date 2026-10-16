package com.sokol.springcourse.Project2Boot;

import com.sokol.springcourse.Project2Boot.models.Book;
import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.repositories.BookRepository;
import com.sokol.springcourse.Project2Boot.repositories.PeopleRepository;
import com.sokol.springcourse.Project2Boot.services.BookService;
import java.time.LocalDateTime;
import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercises rendered workflows. */
@SpringBootTest
@AutoConfigureMockMvc
class LibraryWebTest {
    @Autowired private MockMvc mvc;
    @Autowired private BookRepository books;
    @Autowired private PeopleRepository people;
    @Autowired private BookService lending;

    /** Clears scenario data. */
    @BeforeEach
    void clearData() {
        books.deleteAllInBatch();
        people.deleteAllInBatch();
    }

    @Test
    void landingPageOpensCatalog() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/books"));
        mvc.perform(get("/books")).andExpect(status().isOk()).andExpect(content().string(containsString("No books")));
    }

    @Test
    void creationFormsContainRealCsrfFields() throws Exception {
        mvc.perform(get("/books/new")).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/people/new")).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void mutationWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/books").param("title", "Title").param("author", "Author").param("year", "2000"))
                .andExpect(status().isForbidden());
        assertEquals(0, books.count());
    }

    @Test
    void creatingBookDoesNotAcceptIdentityOrLoanFields() throws Exception {
        Book original = book("Existing");
        Person reader = reader("Alex Reader");
        mvc.perform(post("/books").with(csrf()).param("title", "Added").param("author", "Author")
                        .param("year", "2000").param("id", String.valueOf(original.getId()))
                        .param("person.id", String.valueOf(reader.getId())).param("bookingTime", "2000-01-01T00:00:00"))
                .andExpect(status().is3xxRedirection());
        assertEquals(2, books.count());
        Book added = books.findAll().stream().filter(b -> b.getTitle().equals("Added")).findFirst().orElseThrow();
        assertNull(added.getPerson());
        assertNull(added.getBookingTime());
        assertEquals("Existing", books.findById(original.getId()).orElseThrow().getTitle());
    }

    @Test
    void blankBookAndFutureYearReturnFormErrorsWithoutSaving() throws Exception {
        mvc.perform(post("/books").with(csrf()).param("title", "   ").param("author", "Author")
                        .param("year", Year.now().plusYears(1).toString()))
                .andExpect(status().isOk()).andExpect(view().name("books/new"))
                .andExpect(model().attributeHasFieldErrors("book", "title", "year"));
        assertEquals(0, books.count());
    }

    @Test
    void editingBorrowedBookPreservesLoanAndIgnoresTampering() throws Exception {
        Book book = book("Original");
        Person reader = reader("Alex Reader");
        lending.assign(book.getId(), reader.getId());
        LocalDateTime checkout = books.findById(book.getId()).orElseThrow().getBookingTime();
        mvc.perform(patch("/books/{id}", book.getId()).with(csrf()).param("title", "Edited")
                        .param("author", "Author").param("year", "2001").param("id", "999")
                        .param("person.id", "999").param("bookingTime", "2000-01-01T00:00:00"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/books/" + book.getId()));
        Book changed = books.findById(book.getId()).orElseThrow();
        assertEquals("Edited", changed.getTitle());
        assertEquals(reader.getId(), changed.getPerson().getId());
        assertEquals(checkout, changed.getBookingTime());
        mvc.perform(get("/books/{id}", book.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Reader")));
    }

    @Test
    void checkoutAndReturnWorkThroughProtectedForms() throws Exception {
        Book book = book("Loan");
        Person reader = reader("Alex Reader");
        mvc.perform(patch("/books/assign").with(csrf()).param("book_id", String.valueOf(book.getId()))
                        .param("person_id", String.valueOf(reader.getId())))
                .andExpect(status().is3xxRedirection());
        assertNotNull(books.findById(book.getId()).orElseThrow().getBookingTime());
        mvc.perform(get("/people/{id}", reader.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Loan")));
        mvc.perform(patch("/books/free").with(csrf()).param("book_id", String.valueOf(book.getId())))
                .andExpect(status().is3xxRedirection());
        Book returned = books.findById(book.getId()).orElseThrow();
        assertNull(returned.getPerson());
        assertNull(returned.getBookingTime());
    }

    @Test
    void reassignmentAndDeletionCannotDestroyAnActiveLoan() throws Exception {
        Book book = book("Loan");
        Person first = reader("First Reader");
        Person second = reader("Second Reader");
        lending.assign(book.getId(), first.getId());
        mvc.perform(patch("/books/assign").with(csrf()).param("book_id", String.valueOf(book.getId()))
                        .param("person_id", String.valueOf(second.getId())))
                .andExpect(status().isConflict());
        mvc.perform(delete("/books/{id}", book.getId()).with(csrf())).andExpect(status().isConflict());
        mvc.perform(delete("/people/{id}", first.getId()).with(csrf())).andExpect(status().isConflict());
        assertEquals(first.getId(), books.findById(book.getId()).orElseThrow().getPerson().getId());
        assertTrue(people.existsById(first.getId()));
    }

    @Test
    void availableBookAndReaderCanBeDeleted() throws Exception {
        Book book = book("Available");
        Person reader = reader("Reader");
        mvc.perform(delete("/books/{id}", book.getId()).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(delete("/people/{id}", reader.getId()).with(csrf())).andExpect(status().is3xxRedirection());
        assertEquals(0, books.count());
        assertEquals(0, people.count());
    }

    @Test
    void readerNameCanStayUnchangedButCannotBecomeAnotherName() throws Exception {
        Person reader = reader("Alex Reader");
        reader("Taken Name");
        mvc.perform(patch("/people/{id}", reader.getId()).with(csrf()).param("name", "Alex Reader")
                        .param("yearOfBirth", "1981").param("id", "999"))
                .andExpect(status().is3xxRedirection());
        assertEquals("1981", people.findById(reader.getId()).orElseThrow().getYearOfBirth());
        mvc.perform(patch("/people/{id}", reader.getId()).with(csrf()).param("name", "Taken Name")
                        .param("yearOfBirth", "1981"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("person", "name"));
        assertEquals("Alex Reader", people.findById(reader.getId()).orElseThrow().getName());
    }

    @Test
    void duplicateReaderCreationDoesNotInsertAnotherRow() throws Exception {
        reader("Alex Reader");
        mvc.perform(post("/people").with(csrf()).param("name", "Alex Reader").param("yearOfBirth", "1980"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("person", "name"));
        assertEquals(1, people.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/books/99999", "/people/99999", "/books/99999/edit", "/people/99999/edit"})
    void missingRecordProducesFriendlyNotFound(String url) throws Exception {
        mvc.perform(get(url)).andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("java.lang"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=-1", "books_per_page=0", "books_per_page=101",
            "page=2147483647&books_per_page=100"})
    void invalidPagingProducesBadRequest(String query) throws Exception {
        mvc.perform(get("/books?" + query)).andExpect(status().isBadRequest());
    }

    @Test
    void pagingAndSearchRenderEscapedStoredTitles() throws Exception {
        book("<script>alert(1)</script>");
        book("Zebra book");
        mvc.perform(get("/books").param("page", "0").param("books_per_page", "1"))
                .andExpect(status().isOk()).andExpect(model().attribute("books", hasSize(1)))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
        mvc.perform(get("/books/search").param("q", "ZEBRA")).andExpect(status().isOk())
                .andExpect(model().attribute("books", hasSize(1))).andExpect(content().string(containsString("Zebra book")));
        mvc.perform(get("/books/search")).andExpect(status().isOk()).andExpect(model().attribute("books", hasSize(0)));
    }

    @Test
    void getCannotAssignBook() throws Exception {
        Book book = book("Unchanged");
        Person reader = reader("Alex Reader");
        mvc.perform(get("/books/assign").param("book_id", String.valueOf(book.getId()))
                        .param("person_id", String.valueOf(reader.getId())))
                .andExpect(status().is4xxClientError());
        assertNull(books.findById(book.getId()).orElseThrow().getPerson());
    }

    /** Creates a fixture. */
    private Book book(String title) { return books.saveAndFlush(new Book(0, title, "Author", "2000")); }

    /** Creates a reader. */
    private Person reader(String name) { return people.saveAndFlush(new Person(0, name, "1980")); }
}
