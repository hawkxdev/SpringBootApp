package com.sokol.springcourse.Project2Boot.util;

import com.sokol.springcourse.Project2Boot.models.Person;
import com.sokol.springcourse.Project2Boot.services.PeopleService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Validates reader uniqueness. */
@Component
public class PersonValidator implements Validator {
    private final PeopleService people;

    /** Receives reader lookup. */
    public PersonValidator(PeopleService people) { this.people = people; }

    /** Selects reader forms. */
    @Override
    public boolean supports(Class<?> type) { return Person.class.equals(type); }

    /** Rejects another reader. */
    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;
        if (person.getName() != null && people.findByName(person.getName())
                .filter(existing -> existing.getId() != person.getId()).isPresent()) {
            errors.rejectValue("name", "duplicate", "Reader name already exists");
        }
    }
}
