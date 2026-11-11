
package com.eplaceapi.eplace_backend;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.loginWithPassword(request)
        );
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {

        return ResponseEntity.ok(
                authService.loginWithGoogle(request)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.initiateForgotPassword(request);

        return ResponseEntity.ok(
                "Recovery email successfully dispatched."
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                "Password updated successfully."
        );
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, String>> sendVerificationCode(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email is required.")
            );
        }

        authService.sendVerificationCode(email);

        return ResponseEntity.ok(
                Map.of("message", "Verification code sent successfully.")
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null ||
                email.isBlank() || code.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Email and verification code are required.")
            );
        }

        authService.verifyAccount(email, code);

        return ResponseEntity.ok(
                Map.of("message", "Email verified successfully!")
        );
    }
}
