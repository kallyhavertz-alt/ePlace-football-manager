package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.UserRegistrationRequest;
import com.eplaceapi.eplace_backend.UserResponse;
import com.eplaceapi.eplace_backend.User;
import com.eplaceapi.eplace_backend.UserRepository;
import com.eplaceapi.eplace_backend.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        // 1. Structural Security Age Restriction Check
        if (Period.between(request.dateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("You must be 18 years or older to participate in eplace tournaments.");
        }

        // 2. Prevent Identity Duplication Loops
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("This PES username is already registered.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("This email address is already in use.");
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("This phone number is already registered.");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setDateOfBirth(request.dateOfBirth());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getCreatedAt()
        );
    }
}