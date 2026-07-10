package com.library.management.exception;

import com.library.management.controller.BookController;
import com.library.management.dto.request.BookRequestDTO;
import com.library.management.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/v1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/v1/books/99"));
    }

    @Test
    void shouldReturn409WhenIsbnIsDuplicate() throws Exception {
        BookRequestDTO request = validRequest();
        when(bookService.createBook(any(BookRequestDTO.class)))
                .thenThrow(new DuplicateIsbnException(request.getIsbn()));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "A book with ISBN '978-0132350884' already exists"));
    }

    @Test
    void shouldReturn400WhenBookDataIsInvalid() throws Exception {
        BookRequestDTO request = validRequest();
        request.setAvailableCopies(50);

        when(bookService.updateBook(eq(1L), any(BookRequestDTO.class)))
                .thenThrow(new InvalidBookDataException(
                        "Available copies (50) cannot exceed total copies (10)"));

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Available copies (50) cannot exceed total copies (10)"));
    }

    @Test
    void shouldReturn400WithFieldErrorsOnValidationFailure() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder().build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"))
                .andExpect(jsonPath("$.fieldErrors.author").value("Author is required"))
                .andExpect(jsonPath("$.fieldErrors.isbn").value("ISBN is required"));
    }

    @Test
    void shouldReturn400WhenPathVariableTypeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/books/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
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
}
