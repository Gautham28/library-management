package com.library.management.service;

import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookResponseDTO createBook(BookRequestDTO request);

    BookResponseDTO getBookById(Long id);

    PagedResponse<BookResponseDTO> getAllBooks(Pageable pageable);

    PagedResponse<BookResponseDTO> searchBooks(
            String title,
            String author,
            String genre,
            String isbn,
            Pageable pageable
    );

    BookResponseDTO updateBook(Long id, BookRequestDTO request);

    void deleteBook(Long id);
}
