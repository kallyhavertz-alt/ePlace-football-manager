package com.eplaceapi.eplace_backend;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private String bio;
    private String location;
    private Integer efootballStrength;
    private Integer friendCount;

    private Integer createdLeaguesLimit;
    private Integer createdLeaguesCount;
    private Integer leaguesPlayed;
    private Integer leaguesWon;
    private Integer leaguesNeverWon;
    private Double winProbabilityPercentage;
    private Double lossProbabilityPercentage;
}