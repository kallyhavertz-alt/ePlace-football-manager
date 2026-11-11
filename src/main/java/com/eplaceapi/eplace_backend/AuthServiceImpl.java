/*package com.eplaceapi.eplace_backend;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final org.springframework.mail.javamail.JavaMailSender mailSender;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository; // <-- Injected to create/manage UserProfile
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final String googleClientId = "910119612022-kpmtlbvtnd1msto82beg0j7lmqcnqd1h.apps.googleusercontent.com";

    public AuthServiceImpl(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           JwtService jwtService,
                           MailSender mailSender,
                           PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.mailSender = (org.springframework.mail.javamail.JavaMailSender) mailSender;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public AuthResponse loginWithPassword(LoginRequest request) {
        User user;

        if (request.email() != null && !request.email().isBlank()) {
            user = userRepository.findByEmail(request.email().toLowerCase().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        } else if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user = userRepository.findByPhoneNumber(request.phoneNumber().toLowerCase().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid phone number or password."));
        } else {
            throw new IllegalArgumentException("Please provide either an email or a phone number.");
        }

        // Verify password
        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), token, user.getPhoneNumber());
    }

    @Override
    @Transactional
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
            String googleId = payload.getSubject();
            String googlePictureUrl = (String) payload.get("picture"); // Extract profile picture from Google payload

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setUsername("pes_" + UUID.randomUUID().toString().substring(0, 8));
                        newUser.setGoogleId(googleId);
                        newUser.setVerified(true);
                        return userRepository.save(newUser);
                    });

            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }

            // Ensure a UserProfile entity exists so profile editing, stats, and avatars work identically to password login
            userProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
                UserProfile newProfile = new UserProfile();
                newProfile.setUser(user);
                newProfile.setEfootballStrength(3000);
                newProfile.setLocation("Nairobi, Kenya");
                newProfile.setBio("PES Master");
                if (googlePictureUrl != null && !googlePictureUrl.isBlank()) {
                    newProfile.setProfilePictureUrl(googlePictureUrl);
                }
                return userProfileRepository.save(newProfile);
            });

            String jwtToken = jwtService.generateToken(user);
            return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), jwtToken, user.getPhoneNumber());

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed structural checks: " + e.getMessage());
        }
    }

    @Override
    @Transactional
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
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
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
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
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

    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));

        if (user.isVerified()) {
            throw new IllegalStateException("Account is already verified.");
        }

        String sixDigitCode = String.format("%06d", new Random().nextInt(900000) + 100000);

        user.setVerificationCode(sixDigitCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom("eplacemanager@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject("eplace - Email Verification Code");
            message.setText("Hello " + user.getUsername() + ",\n\n"
                    + "Welcome to eplace! Please use the following 6-digit code to verify your email address:\n"
                    + "Verification Code: " + sixDigitCode + "\n\n"
                    + "This code will expire in 10 minutes.");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to transmit email verification code: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void verifyAccount(String email, String code) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found with this email."));

        if (user.isVerified()) {
            throw new IllegalStateException("Account is already verified.");
        }
        if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }
        if (!user.getVerificationCode().equals(code.trim())) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }
}

 */
package com.eplaceapi.eplace_backend;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final org.springframework.mail.javamail.JavaMailSender mailSender;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final String googleClientId = "910119612022-kpmtlbvtnd1msto82beg0j7lmqcnqd1h.apps.googleusercontent.com";

    public AuthServiceImpl(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           JwtService jwtService,
                           MailSender mailSender,
                           PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.mailSender = (org.springframework.mail.javamail.JavaMailSender) mailSender;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public AuthResponse loginWithPassword(LoginRequest request) {
        User user;

        if (request.email() != null && !request.email().isBlank()) {
            user = userRepository.findByEmail(request.email().toLowerCase().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        } else if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user = userRepository.findByPhoneNumber(request.phoneNumber().toLowerCase().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid phone number or password."));
        } else {
            throw new IllegalArgumentException("Please provide either an email or a phone number.");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), token, user.getPhoneNumber());
    }

    @Override
    @Transactional
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
            String googleId = payload.getSubject();
            String googlePictureUrl = (String) payload.get("picture");

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setUsername("pes_" + UUID.randomUUID().toString().substring(0, 8));
                        newUser.setGoogleId(googleId);
                        newUser.setVerified(true);
                        return userRepository.save(newUser);
                    });

            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }

            userProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
                UserProfile newProfile = new UserProfile();
                newProfile.setUser(user);
                newProfile.setEfootballStrength(3000);
                newProfile.setLocation("Nairobi, Kenya");
                newProfile.setBio("PES Master");
                if (googlePictureUrl != null && !googlePictureUrl.isBlank()) {
                    newProfile.setProfilePictureUrl(googlePictureUrl);
                }
                newProfile.setCreatedLeaguesLimit(50);
                newProfile.setCreatedLeaguesCount(0);
                newProfile.setLeaguesPlayed(0);
                newProfile.setLeaguesWon(0);
                return userProfileRepository.save(newProfile);
            });

            String jwtToken = jwtService.generateToken(user);
            return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), jwtToken, user.getPhoneNumber());

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed structural checks: " + e.getMessage());
        }
    }

    @Override
    @Transactional
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
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
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
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
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

    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));

        if (user.isVerified()) {
            throw new IllegalStateException("Account is already verified.");
        }

        String sixDigitCode = String.format("%06d", new Random().nextInt(900000) + 100000);

        user.setVerificationCode(sixDigitCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom("eplacemanager@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject("eplace - Email Verification Code");
            message.setText("Hello " + user.getUsername() + ",\n\n"
                    + "Welcome to eplace! Please use the following 6-digit code to verify your email address:\n"
                    + "Verification Code: " + sixDigitCode + "\n\n"
                    + "This code will expire in 10 minutes.");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to transmit email verification code: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void verifyAccount(String email, String code) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found with this email."));

        if (user.isVerified()) {
            throw new IllegalStateException("Account is already verified.");
        }
        if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }
        if (!user.getVerificationCode().equals(code.trim())) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }
}