package com.sokol.springcourse.Project2Boot.repositories;

import com.sokol.springcourse.Project2Boot.models.Book;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persists library books. */
public interface BookRepository extends JpaRepository<Book, Integer> {
    /** Loads borrower details. */
    @Override
    @EntityGraph(attributePaths = "person")
    Optional<Book> findById(Integer id);

    /** Lists bounded results. */
    @Override
    @EntityGraph(attributePaths = "person")
    Page<Book> findAll(Pageable pageable);

    /** Lists reader loans. */
    List<Book> findByPersonIdOrderByTitleAsc(int personId);

    /** Checks outstanding loans. */
    boolean existsByPersonId(int personId);

    /** Searches bounded titles. */
    @EntityGraph(attributePaths = "person")
    List<Book> findByTitleContainingIgnoreCase(String query, Pageable pageable);

    /** Locks mutable state. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select book from Book book where book.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") int id);
}
