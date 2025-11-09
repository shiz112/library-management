package com.example.library.web;

import com.example.library.domain.Book;
import com.example.library.domain.Issuance;
import com.example.library.domain.UserAccount;
import com.example.library.repository.BookRepository;
import com.example.library.repository.IssuanceRepository;
import com.example.library.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backup")
public class BackupController {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final IssuanceRepository issuanceRepository;

    public BackupController(BookRepository bookRepository, UserRepository userRepository, IssuanceRepository issuanceRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.issuanceRepository = issuanceRepository;
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping
    public Map<String, Object> exportAll() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("books", bookRepository.findAll());
        payload.put("users", userRepository.findAll());
        payload.put("issuances", issuanceRepository.findAll());
        return payload;
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping
    public ResponseEntity<?> importAll(@RequestBody Map<String, Object> data) {
        // Very simple import: expects arrays keyed by books/users/issuances with same fields
        // In real production, use DTOs and validation; here we rely on Jackson to bind to entities
        @SuppressWarnings("unchecked") List<Book> books = (List<Book>) data.get("books");
        @SuppressWarnings("unchecked") List<UserAccount> users = (List<UserAccount>) data.get("users");
        @SuppressWarnings("unchecked") List<Issuance> issuances = (List<Issuance>) data.get("issuances");
        if (books != null) bookRepository.saveAll(books);
        if (users != null) userRepository.saveAll(users);
        if (issuances != null) issuanceRepository.saveAll(issuances);
        return ResponseEntity.ok().build();
    }
}


