package com.sokol.springcourse.Project2Boot.services;

import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.repositories.BookRepository;
import com.sokol.springcourse.Project2Boot.repositories.PeopleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Coordinates reader records. */
@Service
@Transactional(readOnly = true)
public class PeopleService {
    private final PeopleRepository people;
    private final BookRepository books;

    /** Receives reader repositories. */
    public PeopleService(PeopleRepository people, BookRepository books) {
        this.people = people;
        this.books = books;
    }

    /** Lists sorted readers. */
    public List<Person> findAll() {
        return people.findAll(Sort.by("name").and(Sort.by("id")));
    }

    /** Finds reader details. */
    public Person getById(int id) {
        return people.findById(id).orElseThrow(() -> missingReader());
    }

    /** Creates unique readers. */
    @Transactional
    public Person save(Person form) {
        checkName(form.getName(), 0);
        return people.save(new Person(0, form.getName(), form.getYearOfBirth()));
    }

    /** Updates existing readers. */
    @Transactional
    public void update(int id, Person form) {
        Person person = lockedReader(id);
        checkName(form.getName(), id);
        person.setName(form.getName());
        person.setYearOfBirth(form.getYearOfBirth());
        people.save(person);
    }

    /** Deletes readers without loans. */
    @Transactional
    public void deleteById(int id) {
        Person person = lockedReader(id);
        if (books.existsByPersonId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Return all books before deleting this reader");
        }
        people.delete(person);
    }

    /** Finds matching names. */
    public Optional<Person> findByName(String name) {
        return people.findByName(name);
    }

    /** Locks existing readers. */
    private Person lockedReader(int id) {
        return people.findByIdForUpdate(id).orElseThrow(() -> missingReader());
    }

    /** Checks name ownership. */
    private void checkName(String name, int id) {
        if (people.existsByNameAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reader name already exists");
        }
    }

    /** Describes missing readers. */
    private static ResponseStatusException missingReader() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Reader not found");
    }
}
