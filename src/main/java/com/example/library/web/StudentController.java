package com.example.library.web;

import com.example.library.domain.UserAccount;
import com.example.library.repository.UserRepository;
import com.example.library.service.IssuanceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class StudentController {
    private final IssuanceService issuanceService;
    private final UserRepository userRepository;

    public StudentController(IssuanceService issuanceService, UserRepository userRepository) {
        this.issuanceService = issuanceService;
        this.userRepository = userRepository;
    }

    @GetMapping("/issuances")
    public Object myIssuances(@AuthenticationPrincipal User principal) {
        UserAccount ua = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return issuanceService.listByUser(ua);
    }
}


