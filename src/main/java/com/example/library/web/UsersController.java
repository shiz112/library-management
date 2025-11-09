package com.example.library.web;
import com.example.library.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

record PublicUser(Long id, String username, java.util.Set<?> roles) {}

@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserRepository userRepository;

    public UsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // return non-sensitive public user info (no password/email)
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping
    public List<PublicUser> list() {
        return userRepository.findAll().stream()
                .map(u -> new PublicUser(u.getId(), u.getUsername(), u.getRoles()))
                .collect(Collectors.toList());
    }
}
