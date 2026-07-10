package com.library.management.controller;

import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.dto.response.ErrorResponse;
import com.library.management.dto.response.PagedResponse;
import com.library.management.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Books", description = "CRUD and search operations for library books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Create a book", description = "Creates a new book in the library catalog")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or invalid book data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "ISBN already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<BookResponseDTO>> createBook(
            @Valid @RequestBody BookRequestDTO request
    ) {
        BookResponseDTO created = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully", created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieves a single book by its unique identifier")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBookById(
            @Parameter(description = "Book ID", example = "1")
            @PathVariable Long id
    ) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book));
    }

    @GetMapping
    @Operation(
            summary = "List or search books",
            description = """
                    Returns a paginated list of books.
                    Optional filters: title, author, genre, isbn.
                    Pagination: page, size, sort (e.g. sort=title,asc).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<PagedResponse<BookResponseDTO>>> getBooks(
            @Parameter(description = "Filter by title (partial, case-insensitive)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Filter by author (partial, case-insensitive)")
            @RequestParam(required = false) String author,
            @Parameter(description = "Filter by genre (partial, case-insensitive)")
            @RequestParam(required = false) String genre,
            @Parameter(description = "Filter by ISBN (partial, case-insensitive)")
            @RequestParam(required = false) String isbn,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PagedResponse<BookResponseDTO> books = hasSearchFilters(title, author, genre, isbn)
                ? bookService.searchBooks(title, author, genre, isbn, pageable)
                : bookService.getAllBooks(pageable);

        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Fully updates an existing book by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or invalid book data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "ISBN already exists on another book",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<BookResponseDTO>> updateBook(
            @Parameter(description = "Book ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO request
    ) {
        BookResponseDTO updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Deletes a book by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Book deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Book ID", example = "1")
            @PathVariable Long id
    ) {
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
