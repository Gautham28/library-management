package com.library.management.entity;

import com.library.management.enums.BookStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveBook() {
        Book book = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .genre("Software Engineering")
                .isbn("978-0132350884")
                .publisher("Prentice Hall")
                .publishedYear(2008)
                .totalCopies(10)
                .availableCopies(10)
                .price(new BigDecimal("39.99"))
                .description("A handbook of agile software craftsmanship")
                .status(BookStatus.AVAILABLE)
                .build();

        entityManager.persist(book);
        entityManager.flush();
        entityManager.clear();

        Book found = entityManager.find(Book.class, book.getId());

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Clean Code");
        assertThat(found.getIsbn()).isEqualTo("978-0132350884");
        assertThat(found.getStatus()).isEqualTo(BookStatus.AVAILABLE);
    }
}
