package com.library.management.controller;

import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.dto.response.PagedResponse;
import com.library.management.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponseDTO>> createBook(
            @Valid @RequestBody BookRequestDTO request
    ) {
        BookResponseDTO created = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBookById(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookResponseDTO>>> getBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String isbn,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PagedResponse<BookResponseDTO> books = hasSearchFilters(title, author, genre, isbn)
                ? bookService.searchBooks(title, author, genre, isbn, pageable)
                : bookService.getAllBooks(pageable);

        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO request
    ) {
        BookResponseDTO updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    private boolean hasSearchFilters(String title, String author, String genre, String isbn) {
        return isPresent(title) || isPresent(author) || isPresent(genre) || isPresent(isbn);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
