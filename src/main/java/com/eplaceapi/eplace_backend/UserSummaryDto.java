package com.eplaceapi.eplace_backend;



import lombok.Data;

@Data
public class UserSummaryDto {

    private Long id;
    private String username;
    private boolean isOnline;
    private String lastSeenText;
}
