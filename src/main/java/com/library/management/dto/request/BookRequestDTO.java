package com.library.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a book")
public class BookRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    @Schema(description = "Book title", example = "Clean Code")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must be at most 255 characters")
    @Schema(description = "Author name", example = "Robert C. Martin")
    private String author;

    @NotBlank(message = "Genre is required")
    @Size(max = 100, message = "Genre must be at most 100 characters")
    @Schema(description = "Book genre", example = "Software Engineering")
    private String genre;

    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN must be at most 20 characters")
    @Schema(description = "International Standard Book Number", example = "978-0132350884")
    private String isbn;

    @Size(max = 255, message = "Publisher must be at most 255 characters")
    @Schema(description = "Publisher name", example = "Prentice Hall")
    private String publisher;

    @NotNull(message = "Published year is required")
    @Min(value = 1000, message = "Published year must be a valid year")
    @Max(value = 2100, message = "Published year must be a valid year")
    @Schema(description = "Year the book was published", example = "2008")
    private Integer publishedYear;

    @NotNull(message = "Total copies is required")
    @Min(value = 0, message = "Total copies cannot be negative")
    @Schema(description = "Total number of copies owned", example = "10")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    @Schema(description = "Copies currently available (defaults to totalCopies if omitted)", example = "10")
    private Integer availableCopies;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Schema(description = "Book price", example = "39.99")
    private BigDecimal price;

    @Size(max = 5000, message = "Description must be at most 5000 characters")
    @Schema(description = "Book description", example = "A handbook of agile software craftsmanship")
    private String description;
}
