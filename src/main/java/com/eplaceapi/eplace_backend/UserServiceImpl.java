package com.eplaceapi.eplace_backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (Period.between(request.dateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("You must be 18 years or older to participate in eplace tournaments.");
        }

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

        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        user.setVerificationCode(otpCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        user.setVerified(false);

        User savedUser = userRepository.save(user);

        UserProfile newProfile = new UserProfile();
        newProfile.setUser(savedUser);
        newProfile.setBio("ePlace Competitor");
        newProfile.setLocation("Nairobi, Kenya");
        newProfile.setEfootballStrength(3000);
        newProfile.setCreatedLeaguesLimit(50);
        newProfile.setCreatedLeaguesCount(0);
        newProfile.setLeaguesPlayed(0);
        newProfile.setLeaguesWon(0);
        userProfileRepository.save(newProfile);

        return mapToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse completeGoogleUserDetails(Long userId, CompleteGoogleRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));


        if (request.dateOfBirth() != null) {
            if (Period.between(request.dateOfBirth(), LocalDate.now()).getYears() < 18) {
                throw new IllegalArgumentException("You must be 18 years or older to participate in eplace tournaments.");
            }
            user.setDateOfBirth(request.dateOfBirth());
        }

        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            String cleanPhone = request.phoneNumber().trim();
            if (!cleanPhone.equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(cleanPhone)) {
                throw new IllegalArgumentException("This phone number is already registered to another eplace account.");
            }
            user.setPhoneNumber(cleanPhone);
        }

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.newPassword().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters long.");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword().trim()));
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
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