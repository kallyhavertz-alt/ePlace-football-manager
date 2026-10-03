package com.eplaceapi.eplace_backend;

import lombok.Data;

@Data
public class UpdateProfileRequestDTO {
    private String bio;
    private String location;
    private Integer efootballStrength;
}
