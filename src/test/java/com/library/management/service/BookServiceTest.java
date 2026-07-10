package com.library.management.service;

import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.dto.response.PagedResponse;
import com.library.management.entity.Book;
import com.library.management.enums.BookStatus;
import com.library.management.exception.BookNotFoundException;
import com.library.management.exception.DuplicateIsbnException;
import com.library.management.exception.InvalidBookDataException;
import com.library.management.mapper.BookMapper;
import com.library.management.repository.BookRepository;
import com.library.management.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookRequestDTO request;
    private Book book;
    private BookResponseDTO response;

    @BeforeEach
    void setUp() {
        request = BookRequestDTO.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .genre("Software")
                .isbn("978-0132350884")
                .publisher("Prentice Hall")
                .publishedYear(2008)
                .totalCopies(10)
                .availableCopies(10)
                .price(new BigDecimal("39.99"))
                .description("A handbook of agile software craftsmanship")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .genre("Software")
                .isbn("978-0132350884")
                .publisher("Prentice Hall")
                .publishedYear(2008)
                .totalCopies(10)
                .availableCopies(10)
                .price(new BigDecimal("39.99"))
                .description("A handbook of agile software craftsmanship")
                .status(BookStatus.AVAILABLE)
                .build();

        response = BookResponseDTO.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .genre("Software")
                .isbn("978-0132350884")
                .publisher("Prentice Hall")
                .publishedYear(2008)
                .totalCopies(10)
                .availableCopies(10)
                .price(new BigDecimal("39.99"))
                .description("A handbook of agile software craftsmanship")
                .status(BookStatus.AVAILABLE)
                .build();
    }

    @Test
    void shouldCreateBook() {
        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(bookMapper.toEntity(request)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponseDTO result = bookService.createBook(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository).save(book);
    }

    @Test
    void shouldRejectDuplicateIsbnOnCreate() {
        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(DuplicateIsbnException.class)
                .hasMessageContaining("978-0132350884");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenAvailableCopiesExceedTotal() {
        request.setAvailableCopies(15);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(InvalidBookDataException.class)
                .hasMessageContaining("cannot exceed total copies");

        verify(bookRepository, never()).existsByIsbn(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldGetBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponseDTO result = bookService.getBookById(1L);

        assertThat(result.getIsbn()).isEqualTo("978-0132350884");
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldSearchBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.search(eq("clean"), eq(null), eq(null), eq(null), eq(pageable)))
                .thenReturn(page);
        when(bookMapper.toResponse(book)).thenReturn(response);

        PagedResponse<BookResponseDTO> result = bookService.searchBooks(
                "clean", null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void shouldNormalizeBlankSearchFiltersToNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.search(eq(null), eq(null), eq(null), eq(null), eq(pageable)))
                .thenReturn(page);
        when(bookMapper.toResponse(book)).thenReturn(response);

        bookService.searchBooks("  ", "", null, "   ", pageable);

        verify(bookRepository).search(null, null, null, null, pageable);
    }

    @Test
    void shouldUpdateBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot(request.getIsbn(), 1L)).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponseDTO result = bookService.updateBook(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(bookMapper).updateEntity(book, request);
        verify(bookRepository).save(book);
    }

    @Test
    void shouldRejectDuplicateIsbnOnUpdate() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot(request.getIsbn(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBook(1L, request))
                .isInstanceOf(DuplicateIsbnException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldDeleteBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void shouldThrowWhenDeletingMissingBook() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(BookNotFoundException.class);

        verify(bookRepository, never()).delete(any());
    }
}
