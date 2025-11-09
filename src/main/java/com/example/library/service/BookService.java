package com.example.library.service;

import com.example.library.domain.Book;
import com.example.library.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Page<Book> list(Pageable pageable) { return bookRepository.findAll(pageable); }

    public Optional<Book> get(Long id) { return bookRepository.findById(id); }

    @Transactional
    public Book create(Book book) {
        if (book.getAvailableCopies() == null) book.setAvailableCopies(book.getTotalCopies());
        return bookRepository.save(book);
    }

    @Transactional
    public Optional<Book> update(Long id, Book data) {
        return bookRepository.findById(id).map(b -> {
            b.setTitle(data.getTitle());
            b.setAuthor(data.getAuthor());
            b.setIsbn(data.getIsbn());
            b.setCategory(data.getCategory());
            b.setTotalCopies(data.getTotalCopies());
            b.setAvailableCopies(data.getAvailableCopies());
            return b;
        });
    }

    public void delete(Long id) { bookRepository.deleteById(id); }

    public List<Book> search(String title, String author, String category) {
        if (title != null && !title.isBlank()) return bookRepository.findByTitleContainingIgnoreCase(title);
        if (author != null && !author.isBlank()) return bookRepository.findByAuthorContainingIgnoreCase(author);
        if (category != null && !category.isBlank()) return bookRepository.findByCategoryContainingIgnoreCase(category);
        return bookRepository.findAll();
    }
}


