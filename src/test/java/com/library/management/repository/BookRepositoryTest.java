package com.library.management.repository;

import com.library.management.entity.Book;
import com.library.management.enums.BookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        bookRepository.save(book("Clean Code", "Robert C. Martin", "Software", "978-0132350884"));
        bookRepository.save(book("Effective Java", "Joshua Bloch", "Software", "978-0134685991"));
        bookRepository.save(book("The Hobbit", "J.R.R. Tolkien", "Fantasy", "978-0547928227"));
        bookRepository.save(book("Clean Architecture", "Robert C. Martin", "Software", "978-0134494166"));
    }

    @Test
    void shouldFindByIsbn() {
        assertThat(bookRepository.findByIsbn("978-0132350884"))
                .isPresent()
                .get()
                .extracting(Book::getTitle)
                .isEqualTo("Clean Code");
    }

    @Test
    void shouldDetectExistingIsbn() {
        assertThat(bookRepository.existsByIsbn("978-0132350884")).isTrue();
        assertThat(bookRepository.existsByIsbn("000-0000000000")).isFalse();
    }

    @Test
    void shouldSearchByTitleIgnoreCase() {
        Page<Book> page = bookRepository.findByTitleContainingIgnoreCase(
                "clean", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent())
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");
    }

    @Test
    void shouldSearchByAuthorWithPagination() {
        Page<Book> page = bookRepository.findByAuthorContainingIgnoreCase(
                "Martin", PageRequest.of(0, 1, Sort.by("title").ascending()));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Clean Architecture");
    }

    @Test
    void shouldSearchWithMultipleOptionalFilters() {
        Page<Book> page = bookRepository.search(
                "clean",
                "Martin",
                "Software",
                null,
                PageRequest.of(0, 10, Sort.by("title").ascending())
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Clean Architecture");
    }

    @Test
    void shouldReturnAllWhenAllFiltersAreNull() {
        Page<Book> page = bookRepository.search(
                null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    void shouldFindByStatus() {
        Book outOfStock = book("Sold Out", "Someone", "Fiction", "978-1111111111");
        outOfStock.setAvailableCopies(0);
        outOfStock.setStatus(BookStatus.OUT_OF_STOCK);
        bookRepository.save(outOfStock);

        Page<Book> page = bookRepository.findByStatus(
                BookStatus.OUT_OF_STOCK, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Sold Out");
    }

    private Book book(String title, String author, String genre, String isbn) {
        return Book.builder()
                .title(title)
                .author(author)
                .genre(genre)
                .isbn(isbn)
                .publisher("Test Publisher")
                .publishedYear(2020)
                .totalCopies(5)
                .availableCopies(5)
                .price(new BigDecimal("29.99"))
                .description("Test description")
                .status(BookStatus.AVAILABLE)
                .build();
    }
}
