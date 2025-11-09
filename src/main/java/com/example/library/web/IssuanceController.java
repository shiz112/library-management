package com.example.library.web;

import com.example.library.domain.Issuance;
import com.example.library.service.IssuanceService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

record IssueRequest(Long userId, Long bookId, @Min(1) int loanDays) {}

@RestController
@RequestMapping("/issuances")
public class IssuanceController {
    private final IssuanceService issuanceService;

    public IssuanceController(IssuanceService issuanceService) {
        this.issuanceService = issuanceService;
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping
    public ResponseEntity<?> issue(@RequestBody IssueRequest req) {
        return issuanceService.issueBook(req.userId(), req.bookId(), req.loanDays())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body(Map.of("error", "invalid user/book or unavailable")));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping("/{issuanceId}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long issuanceId) {
        return issuanceService.returnBook(issuanceId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public List<Issuance> active() { return issuanceService.listActive(); }

    @GetMapping("/overdue")
    public List<Issuance> overdue() { return issuanceService.listOverdue(); }
}


