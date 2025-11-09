package com.example.library.repository;

import com.example.library.domain.Issuance;
import com.example.library.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IssuanceRepository extends JpaRepository<Issuance, Long> {
    List<Issuance> findByUser(UserAccount user);
    List<Issuance> findByReturnedDateIsNull();
    List<Issuance> findByDueDateBeforeAndReturnedDateIsNull(LocalDate date);
}


