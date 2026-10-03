package com.eplaceapi.eplace_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${admin.email.recipient:eplacemanager@gmail.com}")
    private String adminEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendAdminLeagueNotification(String username, String leagueName, String amount, String accountCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("🚨 ePlace Alert: New LEAGUE CREATION Request");
            message.setText(String.format(
                    "Hello Admin,\n\n" +
                            "A user is requesting to CREATE a new league and host it on ePlace:\n\n" +
                            "• Host User: %s\n" +
                            "• League Name: %s\n" +
                            "• Account Code (M-Pesa Reference): %s\n" +
                            "• Creation/Stake Fee: KES %s\n\n" +
                            "Action Required:\n" +
                            "Verify M-Pesa receipt for Account Code '%s' (KES %s). Once confirmed, open your Admin Desk to APPROVE or REJECT the league.\n\n" +
                            "ePlace Automated Engine",
                    username, leagueName, accountCode, amount, accountCode, amount
            ));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send admin league creation email: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendAdminJoinNotification(String username, String leagueName, String amount, String accountCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("⚽ ePlace Alert: New PLAYER JOIN Request");
            message.setText(String.format(
                    "Hello Admin,\n\n" +
                            "A player is requesting to JOIN an existing league:\n\n" +
                            "• Player User: %s\n" +
                            "• Target League: %s\n" +
                            "• Account Code (M-Pesa Reference): %s\n" +
                            "• Entry Fee Paid: KES %s\n\n" +
                            "Action Required:\n" +
                            "Check your M-Pesa records for Account Code '%s' matching KES %s. Once verified, confirm their slot approval in the Admin Panel.\n\n" +
                            "ePlace Automated Engine",
                    username, leagueName, accountCode, amount, accountCode, amount
            ));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send admin player join email: " + e.getMessage());
        }
    }
}