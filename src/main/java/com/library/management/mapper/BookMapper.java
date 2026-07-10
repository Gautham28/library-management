package com.library.management.mapper;

import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.entity.Book;
import com.library.management.enums.BookStatus;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toEntity(BookRequestDTO request) {
        int totalCopies = request.getTotalCopies();
        int availableCopies = request.getAvailableCopies() != null
                ? request.getAvailableCopies()
                : totalCopies;

        return Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .genre(request.getGenre())
                .isbn(request.getIsbn())
                .publisher(request.getPublisher())
                .publishedYear(request.getPublishedYear())
                .totalCopies(totalCopies)
                .availableCopies(availableCopies)
                .price(request.getPrice())
                .description(request.getDescription())
                .status(resolveStatus(availableCopies))
                .build();
    }

    public void updateEntity(Book book, BookRequestDTO request) {
        int totalCopies = request.getTotalCopies();
        int availableCopies = request.getAvailableCopies() != null
                ? request.getAvailableCopies()
                : book.getAvailableCopies();

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setPublishedYear(request.getPublishedYear());
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());
        book.setStatus(resolveStatus(availableCopies));
    }

    public BookResponseDTO toResponse(Book book) {
        return BookResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishedYear(book.getPublishedYear())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .price(book.getPrice())
                .description(book.getDescription())
                .status(book.getStatus())
                .build();
    }

    public BookStatus resolveStatus(int availableCopies) {
        return availableCopies > 0 ? BookStatus.AVAILABLE : BookStatus.OUT_OF_STOCK;
    }
}
