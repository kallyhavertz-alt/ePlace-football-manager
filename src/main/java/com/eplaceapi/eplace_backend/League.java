package com.eplaceapi.eplace_backend;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leagues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "account_code", nullable = false, length = 100)
    private String accountCode;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "join_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal joinAmount;

    @Column(name = "max_teams", nullable = false)
    private Integer maxTeams;

    @Column(name = "current_teams", nullable = false)
    private Integer currentTeams = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "award_type", nullable = false)
    private AwardType awardType;

    @Column(name = "creator_username", nullable = false)
    private String creatorUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeagueStatus status = LeagueStatus.PENDING_APPROVAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.currentTeams == null) {
            this.currentTeams = 1;
        }
    }
}