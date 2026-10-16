package com.sokol.springcourse.Project2Boot.repositories;

import com.sokol.springcourse.Project2Boot.models.Person;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persists library readers. */
public interface PeopleRepository extends JpaRepository<Person, Integer> {
    /** Finds duplicate names. */
    Optional<Person> findByName(String name);

    /** Checks another reader. */
    boolean existsByNameAndIdNot(String name, int id);

    /** Locks reader lifecycle. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select person from Person person where person.id = :id")
    Optional<Person> findByIdForUpdate(@Param("id") int id);
}
