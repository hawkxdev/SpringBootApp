package com.sokol.springcourse.Project2Boot.models;

import com.sokol.springcourse.Project2Boot.validation.ValidYear;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** Stores library books. */
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Enter a title")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Enter an author")
    @Size(max = 255)
    @Column(nullable = false)
    private String author;

    @NotBlank(message = "Enter the publication year")
    @ValidYear
    @Column(name = "published_year", nullable = false, length = 4)
    private String year;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @Transient
    private boolean overdue;

    /** Creates an entity. */
    public Book() {}

    /** Initializes book details. */
    public Book(int id, String title, String author, String year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    /** Returns the identifier. */
    public int getId() { return id; }
    /** Sets the identifier. */
    public void setId(int id) { this.id = id; }
    /** Returns the title. */
    public String getTitle() { return title; }
    /** Sets the title. */
    public void setTitle(String title) { this.title = title; }
    /** Returns the author. */
    public String getAuthor() { return author; }
    /** Sets the author. */
    public void setAuthor(String author) { this.author = author; }
    /** Returns publication year. */
    public String getYear() { return year; }
    /** Sets publication year. */
    public void setYear(String year) { this.year = year; }
    /** Returns checkout time. */
    public LocalDateTime getBookingTime() { return bookingTime; }
    /** Sets checkout time. */
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    /** Returns the borrower. */
    public Person getPerson() { return person; }
    /** Sets the borrower. */
    public void setPerson(Person person) { this.person = person; }
    /** Returns evaluated status. */
    public boolean isOverdue() { return overdue; }
    /** Sets evaluated status. */
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}
