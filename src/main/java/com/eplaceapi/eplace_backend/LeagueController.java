package com.eplaceapi.eplace_backend;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leagues")
@CrossOrigin(origins = "*")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }



    @PostMapping("/create-request")
    public ResponseEntity<LeagueResponse> createLeague(
            @Valid @RequestBody LeagueCreateRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long creatorId
    ) {
        LeagueResponse response = leagueService.createLeague(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeagueResponse>> getActiveLeagues() {
        return ResponseEntity.ok(leagueService.getActiveLeagues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueResponse> getLeagueById(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getLeagueById(id));
    }


    @GetMapping
    public ResponseEntity<List<LeagueResponse>> getAllLeagues() {
        return ResponseEntity.ok(leagueService.getAllLeagues());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        return ResponseEntity.ok(leagueService.getAdminDashboardData());
    }

   /* @PostMapping("/requests/{id}/accept")
    public ResponseEntity<LeagueResponse> acceptRequest(@PathVariable String id) {
        Long numericId = Long.parseLong(id.replace("req_", ""));
        return ResponseEntity.ok(leagueService.approveLeague(numericId));
    }

    */
   @PostMapping("/requests/{id}/accept")
   public ResponseEntity<LeagueResponse> acceptRequest(@PathVariable String id) {
       if (id.startsWith("req_join_")) {
           Long joinId = Long.parseLong(id.replace("req_join_", ""));
           return ResponseEntity.ok(leagueService.approveJoinRequest(joinId));
       } else {
           Long leagueId = Long.parseLong(id.replace("req_", ""));
            return ResponseEntity.ok(leagueService.approveLeague(leagueId));
       }
   }

    @PostMapping("/payouts/{id}/confirm")
    public ResponseEntity<Void> confirmPayout(@PathVariable String id) {
        return ResponseEntity.ok().build();
    }


    @GetMapping("/admin/pending")
    public ResponseEntity<List<PendingAdminRequestDto>> getPendingAdminRequests() {
        return ResponseEntity.ok(leagueService.getPendingAdminRequests());
    }

    @PostMapping("/admin/{id}/approve")
    public ResponseEntity<LeagueResponse> approveLeague(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.approveLeague(id));
    }

    @PostMapping("/admin/{id}/reject")
    public ResponseEntity<Void> rejectLeague(@PathVariable Long id) {
        leagueService.rejectLeague(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<LeagueResponse> joinLeague(
            @PathVariable Long id,
            @Valid @RequestBody JoinLeagueRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        LeagueResponse response = leagueService.joinLeague(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LeagueResponse>> searchLeagues(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double maxAmount
    ) {
        return ResponseEntity.ok(leagueService.searchLeaguesByName(name != null ? name : ""));
    }

}