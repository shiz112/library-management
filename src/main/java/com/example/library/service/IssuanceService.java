package com.example.library.service;

import com.example.library.domain.Book;
import com.example.library.domain.Issuance;
import com.example.library.domain.UserAccount;
import com.example.library.repository.BookRepository;
import com.example.library.repository.IssuanceRepository;
import com.example.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class IssuanceService {
    private final IssuanceRepository issuanceRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public IssuanceService(IssuanceRepository issuanceRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.issuanceRepository = issuanceRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Optional<Issuance> issueBook(Long userId, Long bookId, int loanDays) {
        Optional<UserAccount> user = userRepository.findById(userId);
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (user.isEmpty() || bookOpt.isEmpty()) return Optional.empty();
        Book book = bookOpt.get();
        if (book.getAvailableCopies() <= 0) return Optional.empty();

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        Issuance issuance = new Issuance();
        issuance.setUser(user.get());
        issuance.setBook(book);
        issuance.setIssuedDate(LocalDate.now());
        issuance.setDueDate(LocalDate.now().plusDays(loanDays));
        issuance.setFineAmount(0);

        return Optional.of(issuanceRepository.save(issuance));
    }

    @Transactional
    public Optional<Issuance> returnBook(Long issuanceId) {
        return issuanceRepository.findById(issuanceId).map(iss -> {
            if (iss.getReturnedDate() == null) {
                iss.setReturnedDate(LocalDate.now());
                Book book = iss.getBook();
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                computeFine(iss); // compute any final fine on return
            }
            return iss;
        });
    }

    public List<Issuance> listActive() { return issuanceRepository.findByReturnedDateIsNull(); }

    public List<Issuance> listOverdue() { return issuanceRepository.findByDueDateBeforeAndReturnedDateIsNull(LocalDate.now()); }

    public List<Issuance> listByUser(UserAccount user) { return issuanceRepository.findByUser(user); }

    public void computeFine(Issuance issuance) {
        if (issuance.getReturnedDate() == null) {
            long daysOver = Math.max(0, ChronoUnit.DAYS.between(issuance.getDueDate(), LocalDate.now()));
            issuance.setFineAmount((int) daysOver); // 1 unit per day overdue
        } else {
            long daysOver = Math.max(0, ChronoUnit.DAYS.between(issuance.getDueDate(), issuance.getReturnedDate()));
            issuance.setFineAmount((int) daysOver);
        }
    }

    @Transactional
    public void recomputeFinesForAllActive() {
        List<Issuance> active = issuanceRepository.findByReturnedDateIsNull();
        for (Issuance i : active) {
            computeFine(i);
        }
    }
}


