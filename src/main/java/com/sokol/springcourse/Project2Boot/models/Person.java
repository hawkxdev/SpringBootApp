package com.sokol.springcourse.Project2Boot.models;

import com.sokol.springcourse.Project2Boot.validation.ValidYear;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Stores library readers. */
@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Enter a reader name")
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Enter the birth year")
    @ValidYear
    @Column(name = "year_of_birth", nullable = false, length = 4)
    private String yearOfBirth;

    /** Creates an entity. */
    public Person() {}

    /** Initializes reader details. */
    public Person(int id, String name, String yearOfBirth) {
        this.id = id;
        this.name = name;
        this.yearOfBirth = yearOfBirth;
    }

    /** Returns the identifier. */
    public int getId() { return id; }
    /** Sets the identifier. */
    public void setId(int id) { this.id = id; }
    /** Returns the name. */
    public String getName() { return name; }
    /** Sets the name. */
    public void setName(String name) { this.name = name; }
    /** Returns birth year. */
    public String getYearOfBirth() { return yearOfBirth; }
    /** Sets birth year. */
    public void setYearOfBirth(String yearOfBirth) { this.yearOfBirth = yearOfBirth; }
}
