package com.library.management.dto.response;

import com.library.management.enums.BookStatus;
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
public class BookResponseDTO {

    private Long id;
    private String title;
    private String author;
    private String genre;
    private String isbn;
    private String publisher;
    private Integer publishedYear;
    private Integer totalCopies;
    private Integer availableCopies;
    private BigDecimal price;
    private String description;
    private BookStatus status;
}
