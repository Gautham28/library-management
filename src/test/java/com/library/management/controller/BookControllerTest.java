package com.library.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.dto.response.PagedResponse;
import com.library.management.enums.BookStatus;
import com.library.management.exception.BookNotFoundException;
import com.library.management.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void shouldCreateBook() throws Exception {
        BookRequestDTO request = validRequest();
        BookResponseDTO response = sampleResponse();

        when(bookService.createBook(any(BookRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Clean Code"));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder().build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBookById() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isbn").value("978-0132350884"));
    }

    @Test
    void shouldListBooksWithoutFilters() throws Exception {
        PagedResponse<BookResponseDTO> page = PagedResponse.<BookResponseDTO>builder()
                .content(List.of(sampleResponse()))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(bookService).getAllBooks(any(Pageable.class));
    }

    @Test
    void shouldSearchBooksWithFilters() throws Exception {
        PagedResponse<BookResponseDTO> page = PagedResponse.<BookResponseDTO>builder()
                .content(List.of(sampleResponse()))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(bookService.searchBooks(eq("clean"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/books")
                        .param("title", "clean")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Clean Code"));

        verify(bookService).searchBooks(eq("clean"), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        BookRequestDTO request = validRequest();
        when(bookService.updateBook(eq(1L), any(BookRequestDTO.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
    }

    @Test
    void shouldSurfaceBookNotFoundExceptionUntilGlobalHandlerExists() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new BookNotFoundException(99L));

        // Phase 7 will map this to HTTP 404 via @ControllerAdvice.
        // Without it, the exception bubbles out of the dispatcher.
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> mockMvc.perform(get("/api/v1/books/99"))
                )
                .hasCauseInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("99");
    }

    private BookRequestDTO validRequest() {
        return BookRequestDTO.builder()
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
    }

    private BookResponseDTO sampleResponse() {
        return BookResponseDTO.builder()
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
}
