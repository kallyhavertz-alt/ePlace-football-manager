package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.LeagueCreateRequest;
import com.eplaceapi.eplace_backend.LeagueResponse;
import java.util.List;

public interface LeagueService {
    LeagueResponse createLeague(LeagueCreateRequest request, Long creatorId);
    LeagueResponse getLeagueById(Long id);
    List<LeagueResponse> searchLeaguesByName(String name);
    List<LeagueResponse> getAllLeagues();
}