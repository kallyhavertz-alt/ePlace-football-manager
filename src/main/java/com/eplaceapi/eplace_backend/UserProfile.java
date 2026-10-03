package com.eplaceapi.eplace_backend;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private String location;

    @Builder.Default
    private Integer efootballStrength = 3000;

     @Builder.Default
    private Integer createdLeaguesLimit = 50;

    @Builder.Default
    private Integer createdLeaguesCount = 0;

    @Builder.Default
    private Integer leaguesPlayed = 0;

    @Builder.Default
    private Integer leaguesWon = 0;
}
