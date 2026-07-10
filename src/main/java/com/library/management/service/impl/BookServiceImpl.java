package com.library.management.service.impl;

import com.library.management.dto.request.BookRequestDTO;
import com.library.management.dto.response.BookResponseDTO;
import com.library.management.dto.response.PagedResponse;
import com.library.management.entity.Book;
import com.library.management.exception.BookNotFoundException;
import com.library.management.exception.DuplicateIsbnException;
import com.library.management.exception.InvalidBookDataException;
import com.library.management.mapper.BookMapper;
import com.library.management.repository.BookRepository;
import com.library.management.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookResponseDTO createBook(BookRequestDTO request) {
        log.info("Creating book with ISBN: {}", request.getIsbn());

        validateCopyCounts(request);
        ensureIsbnIsUnique(request.getIsbn());

        Book book = bookMapper.toEntity(request);
        Book saved = bookRepository.save(book);

        log.info("Created book with id: {}", saved.getId());
        return bookMapper.toResponse(saved);
    }

    @Override
    public BookResponseDTO getBookById(Long id) {
        log.debug("Fetching book with id: {}", id);
        return bookMapper.toResponse(findBookOrThrow(id));
    }

    @Override
    public PagedResponse<BookResponseDTO> getAllBooks(Pageable pageable) {
        log.debug("Fetching all books - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<BookResponseDTO> page = bookRepository.findAll(pageable)
                .map(bookMapper::toResponse);

        return PagedResponse.from(page);
    }

    @Override
    public PagedResponse<BookResponseDTO> searchBooks(
            String title,
            String author,
            String genre,
            String isbn,
            Pageable pageable
    ) {
        log.debug("Searching books - title={}, author={}, genre={}, isbn={}", title, author, genre, isbn);

        Page<BookResponseDTO> page = bookRepository.search(
                        normalize(title),
                        normalize(author),
                        normalize(genre),
                        normalize(isbn),
                        pageable
                )
                .map(bookMapper::toResponse);

        return PagedResponse.from(page);
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO request) {
        log.info("Updating book with id: {}", id);

        validateCopyCounts(request);

        Book book = findBookOrThrow(id);
        ensureIsbnIsUniqueForUpdate(request.getIsbn(), id);

        bookMapper.updateEntity(book, request);
        Book saved = bookRepository.save(book);

        log.info("Updated book with id: {}", saved.getId());
        return bookMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        log.info("Deleting book with id: {}", id);

        Book book = findBookOrThrow(id);
        bookRepository.delete(book);

        log.info("Deleted book with id: {}", id);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private void ensureIsbnIsUnique(String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new DuplicateIsbnException(isbn);
        }
    }

    private void ensureIsbnIsUniqueForUpdate(String isbn, Long id) {
        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new DuplicateIsbnException(isbn);
        }
    }

    private void validateCopyCounts(BookRequestDTO request) {
        Integer availableCopies = request.getAvailableCopies();
        if (availableCopies != null && availableCopies > request.getTotalCopies()) {
            throw new InvalidBookDataException(
                    "Available copies (" + availableCopies + ") cannot exceed total copies ("
                            + request.getTotalCopies() + ")"
            );
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
