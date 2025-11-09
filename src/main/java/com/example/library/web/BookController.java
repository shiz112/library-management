package com.example.library.web;

import com.example.library.domain.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) { this.bookService = bookService; }

    @GetMapping
    public java.util.List<Book> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        // return the page content as a plain list to make it simpler for the frontend (it expects an array)
        return bookService.list(PageRequest.of(page, size)).getContent();
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam(required = false) String title,
                             @RequestParam(required = false) String author,
                             @RequestParam(required = false) String category) {
        return bookService.search(title, author, category);
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody Book book) { return ResponseEntity.ok(bookService.create(book)); }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Book book) {
        return bookService.update(id, book).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


