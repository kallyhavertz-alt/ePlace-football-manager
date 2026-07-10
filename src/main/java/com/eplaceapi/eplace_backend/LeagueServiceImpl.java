package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.LeagueCreateRequest;
import com.eplaceapi.eplace_backend.LeagueResponse;
import com.eplaceapi.eplace_backend.League;
import com.eplaceapi.eplace_backend.LeagueRepository;
import com.eplaceapi.eplace_backend.LeagueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;

    public LeagueServiceImpl(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Override
    @Transactional
    public LeagueResponse createLeague(LeagueCreateRequest request, Long creatorId) {
        League league = new League();
        league.setName(request.name().trim());
        league.setCreatorId(creatorId);
        league.setJoinAmount(request.joinAmount());
        league.setMaxTeams(request.maxTeams());
        league.setAwardType(request.awardType());

        League savedLeague = leagueRepository.save(league);
        return mapToResponse(savedLeague);
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueResponse getLeagueById(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + id));
        return mapToResponse(league);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> searchLeaguesByName(String name) {
        return leagueRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> getAllLeagues() {
        return leagueRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

     private LeagueResponse mapToResponse(League league) {
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getCreatorId(),
                league.getJoinAmount(),
                league.getMaxTeams(),
                league.getAwardType(),
                league.getCreatedAt()
        );
    }
}