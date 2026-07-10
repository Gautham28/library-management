package com.library.management.repository;

import com.library.management.entity.Book;
import com.library.management.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // --- Derived query methods (Spring builds SQL from the method name) ---

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Book> findByGenreContainingIgnoreCase(String genre, Pageable pageable);

    Page<Book> findByIsbnContainingIgnoreCase(String isbn, Pageable pageable);

    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    // --- Flexible search: any combination of filters (null = ignore that filter) ---

    @Query("""
            SELECT b FROM Book b
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
              AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:author AS string), '%')))
              AND (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', CAST(:genre AS string), '%')))
              AND (:isbn IS NULL OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', CAST(:isbn AS string), '%')))
            """)
    Page<Book> search(
            @Param("title") String title,
            @Param("author") String author,
            @Param("genre") String genre,
            @Param("isbn") String isbn,
            Pageable pageable
    );
}
