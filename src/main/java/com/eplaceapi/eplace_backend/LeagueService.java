package com.eplaceapi.eplace_backend;

import java.util.List;

public interface LeagueService {

    LeagueResponse createLeague(LeagueCreateRequest request, Long creatorId);

    LeagueResponse joinLeague(Long leagueId, JoinLeagueRequest request, Long userId);

    LeagueResponse getLeagueById(Long id);
    List<LeagueResponse> searchLeaguesByName(String name);
    List<LeagueResponse> getAllLeagues();
    List<LeagueResponse> getActiveLeagues();


     List<PendingAdminRequestDto> getPendingAdminRequests();

    LeagueResponse approveLeague(Long id);
    void rejectLeague(Long id);
    AdminDashboardDto getAdminDashboardData();

    LeagueResponse approveJoinRequest(Long joinRequestId);
    void rejectJoinRequest(Long joinRequestId);
}