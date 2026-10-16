package com.sokol.springcourse.Project2Boot.controllers;

import com.sokol.springcourse.Project2Boot.config.LibraryProperties;
import com.sokol.springcourse.Project2Boot.models.Book;
import com.sokol.springcourse.Project2Boot.services.BookService;
import com.sokol.springcourse.Project2Boot.services.PeopleService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/** Handles book pages. */
@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService books;
    private final PeopleService people;
    private final LibraryProperties properties;

    /** Receives page services. */
    public BookController(BookService books, PeopleService people, LibraryProperties properties) {
        this.books = books;
        this.people = people;
        this.properties = properties;
    }

    /** Restricts editable fields. */
    @InitBinder("book")
    public void bindBook(WebDataBinder binder) {
        binder.setAllowedFields("title", "author", "year");
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /** Lists paginated books. */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(name = "books_per_page", required = false) Integer size,
                       @RequestParam(name = "sort_by_year", defaultValue = "false") boolean sortByYear,
                       Model model) {
        Page<Book> result = books.getBooksByPage(page, size == null ? properties.defaultPageSize() : size, sortByYear);
        model.addAttribute("books", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("sortByYear", sortByYear);
        model.addAttribute("maxPageSize", properties.maxPageSize());
        return "books/list";
    }

    /** Opens creation form. */
    @GetMapping("/new")
    public String newForm(@ModelAttribute("book") Book book) { return "books/new"; }

    /** Creates validated books. */
    @PostMapping
    public String create(@Valid @ModelAttribute("book") Book book, BindingResult result) {
        if (result.hasErrors()) { return "books/new"; }
        Book created = books.saveBook(book);
        return "redirect:/books/" + created.getId();
    }

    /** Shows book details. */
    @GetMapping("/{id}")
    public String show(@PathVariable int id, Model model) {
        model.addAttribute("book", books.getBookById(id));
        model.addAttribute("people", people.findAll());
        return "books/show";
    }

    /** Opens metadata editor. */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable int id, Model model) {
        model.addAttribute("book", books.getBookById(id));
        return "books/edit";
    }

    /** Updates validated metadata. */
    @PatchMapping("/{id}")
    public String update(@PathVariable int id, @Valid @ModelAttribute("book") Book book, BindingResult result) {
        books.getBookById(id);
        book.setId(id);
        if (result.hasErrors()) { return "books/edit"; }
        books.updateBook(id, book);
        return "redirect:/books/" + id;
    }

    /** Assigns a borrower. */
    @PatchMapping("/assign")
    public String assign(@RequestParam("person_id") int personId, @RequestParam("book_id") int bookId) {
        books.assign(bookId, personId);
        return "redirect:/books/" + bookId;
    }

    /** Releases a loan. */
    @PatchMapping("/free")
    public String release(@RequestParam("book_id") int bookId) {
        books.release(bookId);
        return "redirect:/books/" + bookId;
    }

    /** Deletes available books. */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        books.deleteBook(id);
        return "redirect:/books";
    }

    /** Searches library titles. */
    @GetMapping("/search")
    public String search(@RequestParam(name = "q", defaultValue = "") String query, Model model) {
        model.addAttribute("searchQuery", query);
        model.addAttribute("books", books.search(query));
        model.addAttribute("maxResults", properties.maxPageSize());
        return "books/search";
    }
}
