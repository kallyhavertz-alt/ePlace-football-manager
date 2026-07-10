package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.LeagueCreateRequest;
import com.eplaceapi.eplace_backend.LeagueResponse;
import com.eplaceapi.eplace_backend.LeagueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @PostMapping
    public ResponseEntity<LeagueResponse> createLeague(
            @Valid @RequestBody LeagueCreateRequest request,
            @RequestHeader("X-User-Id") Long creatorId
    ) {
        LeagueResponse response = leagueService.createLeague(request, creatorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueResponse> getLeagueById(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getLeagueById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LeagueResponse>> searchLeagues(@RequestParam String name) {
        return ResponseEntity.ok(leagueService.searchLeaguesByName(name));
    }

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> getAllLeagues() {
        return ResponseEntity.ok(leagueService.getAllLeagues());
    }
}