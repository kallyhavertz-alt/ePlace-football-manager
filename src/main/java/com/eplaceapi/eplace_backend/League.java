package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.AwardType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leagues")
@Data
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "join_amount", nullable = false)
    private BigDecimal joinAmount;

    @Column(name = "max_teams", nullable = false)
    private Integer maxTeams;

    @Enumerated(EnumType.STRING)
    @Column(name = "award_type", nullable = false)
    private AwardType awardType;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}