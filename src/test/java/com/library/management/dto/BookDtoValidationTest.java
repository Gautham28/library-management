package com.library.management.dto;

import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.entity.Book;
import com.library.management.enums.BookStatus;
import com.library.management.mapper.BookMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookDtoValidationTest {

    private static Validator validator;
    private final BookMapper bookMapper = new BookMapper();

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationForValidRequest() {
        BookRequestDTO request = validRequest();

        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreMissing() {
        BookRequestDTO request = BookRequestDTO.builder().build();

        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(
                        "Title is required",
                        "Author is required",
                        "Genre is required",
                        "ISBN is required",
                        "Published year is required",
                        "Total copies is required",
                        "Price is required"
                );
    }

    @Test
    void shouldFailValidationForNegativeCopiesAndPrice() {
        BookRequestDTO request = validRequest();
        request.setTotalCopies(-1);
        request.setAvailableCopies(-5);
        request.setPrice(new BigDecimal("-10.00"));

        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(
                        "Total copies cannot be negative",
                        "Available copies cannot be negative",
                        "Price cannot be negative"
                );
    }

    @Test
    void shouldMapRequestToEntityAndDefaultAvailableCopies() {
        BookRequestDTO request = validRequest();
        request.setAvailableCopies(null);

        Book book = bookMapper.toEntity(request);

        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAvailableCopies()).isEqualTo(10);
        assertThat(book.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(book.getId()).isNull();
    }

    @Test
    void shouldMapEntityToResponse() {
        Book book = bookMapper.toEntity(validRequest());
        book.setId(1L);

        BookResponseDTO response = bookMapper.toResponse(book);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getIsbn()).isEqualTo("978-0132350884");
        assertThat(response.getStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    void shouldResolveOutOfStockWhenNoCopiesAvailable() {
        assertThat(bookMapper.resolveStatus(0)).isEqualTo(BookStatus.OUT_OF_STOCK);
        assertThat(bookMapper.resolveStatus(3)).isEqualTo(BookStatus.AVAILABLE);
    }

    private BookRequestDTO validRequest() {
        return BookRequestDTO.builder()
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
                .build();
    }
}
