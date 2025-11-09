package com.example.library.config;

import com.example.library.domain.Book;
import com.example.library.domain.Role;
import com.example.library.domain.UserAccount;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(BookRepository bookRepository, UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (bookRepository.count() == 0) {
                Book b1 = new Book(); b1.setTitle("Clean Code"); b1.setAuthor("Robert C. Martin"); b1.setIsbn("9780132350884"); b1.setCategory("Software"); b1.setTotalCopies(5); b1.setAvailableCopies(5);
                Book b2 = new Book(); b2.setTitle("Design Patterns"); b2.setAuthor("GoF"); b2.setIsbn("9780201633610"); b2.setCategory("Software"); b2.setTotalCopies(3); b2.setAvailableCopies(3);
                bookRepository.save(b1);
                bookRepository.save(b2);
            }
            if (userRepository.count() == 0) {
                UserAccount admin = new UserAccount();
                admin.setUsername("librarian");
                admin.setEmail("librarian@example.com");
                admin.setPasswordHash(encoder.encode("password"));
                admin.setRoles(Set.of(Role.LIBRARIAN));
                userRepository.save(admin);

                UserAccount student = new UserAccount();
                student.setUsername("student");
                student.setEmail("student@example.com");
                student.setPasswordHash(encoder.encode("password"));
                student.setRoles(Set.of(Role.STUDENT));
                userRepository.save(student);
            }
        };
    }
}


