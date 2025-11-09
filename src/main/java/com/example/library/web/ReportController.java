package com.example.library.web;

import com.example.library.domain.Book;
import com.example.library.domain.Issuance;
import com.example.library.repository.BookRepository;
import com.example.library.service.IssuanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final BookRepository bookRepository;
    private final IssuanceService issuanceService;

    public ReportController(BookRepository bookRepository, IssuanceService issuanceService) {
        this.bookRepository = bookRepository;
        this.issuanceService = issuanceService;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<Book> allBooks = bookRepository.findAll();
        List<Issuance> active = issuanceService.listActive();
        List<Issuance> overdue = issuanceService.listOverdue();
        long available = allBooks.stream().mapToLong(b -> b.getAvailableCopies()).sum();
        long total = allBooks.stream().mapToLong(b -> b.getTotalCopies()).sum();
        return Map.of(
                "totalBooks", total,
                "availableCopies", available,
                "activeIssuances", active.size(),
                "overdueCount", overdue.size()
        );
    }
}


