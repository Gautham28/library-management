package com.library.management.dto.request;

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
public class BookRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must be at most 255 characters")
    private String author;

    @NotBlank(message = "Genre is required")
    @Size(max = 100, message = "Genre must be at most 100 characters")
    private String genre;

    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN must be at most 20 characters")
    private String isbn;

    @Size(max = 255, message = "Publisher must be at most 255 characters")
    private String publisher;

    @NotNull(message = "Published year is required")
    @Min(value = 1000, message = "Published year must be a valid year")
    @Max(value = 2100, message = "Published year must be a valid year")
    private Integer publishedYear;

    @NotNull(message = "Total copies is required")
    @Min(value = 0, message = "Total copies cannot be negative")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;
}
