package com.example.library.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "issuances", indexes = {
        @Index(name = "idx_issuance_user", columnList = "user_id"),
        @Index(name = "idx_issuance_book", columnList = "book_id"),
        @Index(name = "idx_issuance_dueDate", columnList = "dueDate")
})
public class Issuance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @NotNull
    private LocalDate issuedDate;

    @NotNull
    private LocalDate dueDate;

    private LocalDate returnedDate;

    private Integer fineAmount = 0; // in currency units

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }
    public Integer getFineAmount() { return fineAmount; }
    public void setFineAmount(Integer fineAmount) { this.fineAmount = fineAmount; }
}


