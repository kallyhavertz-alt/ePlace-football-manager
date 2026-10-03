package com.eplaceapi.eplace_backend;

public interface EmailService {
    void sendAdminLeagueNotification(String username, String leagueName, String amount, String accountCode);
    void sendAdminJoinNotification(String username, String leagueName, String amount, String accountCode);
}