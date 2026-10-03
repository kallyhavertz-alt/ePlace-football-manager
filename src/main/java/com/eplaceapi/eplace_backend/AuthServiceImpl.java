package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.*;
import com.eplaceapi.eplace_backend.User;
import com.eplaceapi.eplace_backend.UserRepository;
import com.eplaceapi.eplace_backend.AuthService;
import com.eplaceapi.eplace_backend.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final org.springframework.mail.javamail.JavaMailSender mailSender;
    private final PasswordResetTokenRepository tokenRepository;


    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

     private final String googleClientId = "910119612022-kpmtlbvtnd1msto82beg0j7lmqcnqd1h.apps.googleusercontent.com";

    public AuthServiceImpl(UserRepository userRepository, JwtService jwtService, MailSender mailSender,PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.mailSender = (org.springframework.mail.javamail.JavaMailSender) mailSender;
        this.tokenRepository = tokenRepository;

    }

    @Override
    public AuthResponse loginWithPassword(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), token);
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.idToken());
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google verification token.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail().toLowerCase().trim();
            String googleId = payload.getSubject(); // Safe, unchangeable identity string

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                         User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setUsername("pes_" + UUID.randomUUID().toString().substring(0, 8)); // Temporary holder
                        newUser.setGoogleId(googleId);
                        newUser.setVerified(true);
                        return userRepository.save(newUser);
                    });

            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }

            String jwtToken = jwtService.generateToken(user);
            return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), jwtToken);

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed structural checks: " + e.getMessage());
        }
    }


    @Override
    @org.springframework.transaction.annotation.Transactional
    public void initiateForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("If the account exists, a recovery token has been initialized."));

        tokenRepository.deleteByUser(user);

        java.security.SecureRandom random = new java.security.SecureRandom();
        int codeInt = 100000 + random.nextInt(900000);
        String sixDigitCode = String.valueOf(codeInt);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setCode(sixDigitCode);
        resetToken.setUser(user);
        resetToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(resetToken);

        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom("eplacemanager@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject("eplace - Password Reset Verification Code");
            message.setText("Hello " + user.getUsername() + ",\n\n"
                    + "You requested a password reset for your eplace account.\n"
                    + "Your 6-digit verification code is: " + sixDigitCode + "\n\n"
                    + "This code is highly sensitive and will expire in 15 minutes. Do not share it with anyone.");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to transmit verification email: " + e.getMessage());
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Locate the token provided by the user's input
        PasswordResetToken resetToken = tokenRepository.findByCode(request.token().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}