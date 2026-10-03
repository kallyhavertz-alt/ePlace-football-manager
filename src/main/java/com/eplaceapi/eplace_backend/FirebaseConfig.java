package com.eplaceapi.eplace_backend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() {
        System.out.println(">>> Attempting Firebase initialization...");

        try {
            ClassPathResource serviceAccount = new ClassPathResource("firebase-account.json");

            if (!serviceAccount.exists()) {
                System.err.println("❌ ERROR: firebase-account.json was not found in src/main/resources/");
                return;
            }

            try (InputStream is = serviceAccount.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    System.out.println("✅ Firebase Application Initialized Successfully for ePlace!");
                } else {
                    System.out.println("ℹ️ Firebase is already initialized.");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}