package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.*;

public interface AuthService {
    AuthResponse loginWithPassword(LoginRequest request);
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
    void initiateForgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void sendVerificationCode(String email);
    void verifyAccount(String email, String code);
}