package com.sokol.springcourse.Project2Boot.controllers;

import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.services.BookService;
import com.sokol.springcourse.Project2Boot.services.PeopleService;
import com.sokol.springcourse.Project2Boot.util.PersonValidator;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/** Handles reader pages. */
@Controller
@RequestMapping("/people")
public class PeopleController {
    private final PeopleService people;
    private final BookService books;
    private final PersonValidator validator;

    /** Receives reader services. */
    public PeopleController(PeopleService people, BookService books, PersonValidator validator) {
        this.people = people;
        this.books = books;
        this.validator = validator;
    }

    /** Restricts editable fields. */
    @InitBinder("person")
    public void bindPerson(WebDataBinder binder) {
        binder.setAllowedFields("name", "yearOfBirth");
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /** Lists reader records. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("people", people.findAll());
        return "people/index";
    }

    /** Opens creation form. */
    @GetMapping("/new")
    public String newForm(@ModelAttribute("person") Person person) { return "people/new"; }

    /** Creates validated readers. */
    @PostMapping
    public String create(@Valid @ModelAttribute("person") Person person, BindingResult result) {
        validator.validate(person, result);
        if (result.hasErrors()) { return "people/new"; }
        Person created = people.save(person);
        return "redirect:/people/" + created.getId();
    }

    /** Shows reader loans. */
    @GetMapping("/{id}")
    public String show(@PathVariable int id, Model model) {
        Person person = people.getById(id);
        model.addAttribute("person", person);
        model.addAttribute("books", books.getBooksByPerson(person));
        return "people/show";
    }

    /** Opens reader editor. */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable int id, Model model) {
        model.addAttribute("person", people.getById(id));
        return "people/edit";
    }

    /** Updates validated readers. */
    @PatchMapping("/{id}")
    public String update(@PathVariable int id, @Valid @ModelAttribute("person") Person person, BindingResult result) {
        people.getById(id);
        person.setId(id);
        validator.validate(person, result);
        if (result.hasErrors()) { return "people/edit"; }
        people.update(id, person);
        return "redirect:/people/" + id;
    }

    /** Deletes eligible readers. */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        people.deleteById(id);
        return "redirect:/people";
    }
}
