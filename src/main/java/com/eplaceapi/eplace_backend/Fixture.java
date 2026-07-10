package com.eplaceapi.eplace_backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixtures")
@Data
public class Fixture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "league_id", nullable = false)
    private Long leagueId;

    @Column(name = "home_team_id", nullable = false)
    private Long homeTeamId;

    @Column(name = "away_team_id", nullable = false)
    private Long awayTeamId;

    private Integer homeScore;
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.PENDING;

    @Column(name = "timer_started_at")
    private LocalDateTime timerStartedAt;
}

enum MatchStatus {
    PENDING,
    ACTIVE,
    DISPUTED,
    COMPLETED
}