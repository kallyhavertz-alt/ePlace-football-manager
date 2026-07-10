package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
     List<League> findByNameContainingIgnoreCase(String name);
}