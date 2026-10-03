package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.*;

public interface AuthService {
    AuthResponse loginWithPassword(LoginRequest request);
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
    void initiateForgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}