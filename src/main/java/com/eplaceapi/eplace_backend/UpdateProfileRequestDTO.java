package com.eplaceapi.eplace_backend;

import lombok.Data;

@Data
public class UpdateProfileRequestDTO {
    private String username;
    private String currentPassword;
    private String newPassword;
    private String bio;
    private String location;
    private Integer efootballStrength;
}
