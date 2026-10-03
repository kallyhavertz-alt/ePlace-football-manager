package com.eplaceapi.eplace_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Autowired
    public LeagueServiceImpl(LeagueRepository leagueRepository,
                             EmailService emailService,
                             NotificationService notificationService) {
        this.leagueRepository = leagueRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public LeagueResponse createLeague(LeagueCreateRequest request, Long creatorId) {
        League league = new League();
        league.setName(request.name().trim());
        league.setAccountCode(request.accountCode().trim());
        league.setCreatorId(creatorId);
        league.setCreatorUsername(request.creatorUsername() != null ? request.creatorUsername() : "Player_" + creatorId);
        league.setJoinAmount(request.joinAmount());
        league.setMaxTeams(request.maxTeams());
        league.setCurrentTeams(1);
        league.setAwardType(request.awardType());
        league.setStatus(LeagueStatus.PENDING_APPROVAL);

        League savedLeague = leagueRepository.save(league);

        // Send Email Notification to Admin
        emailService.sendAdminLeagueNotification(
                savedLeague.getCreatorUsername(),
                savedLeague.getName(),
                savedLeague.getJoinAmount().toString(),
                savedLeague.getAccountCode()
        );

        return mapToResponse(savedLeague);
    }

    @Override
    @Transactional
    public LeagueResponse joinLeague(Long leagueId, JoinLeagueRequest request, Long userId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));

        if (league.getCurrentTeams() >= league.getMaxTeams()) {
            throw new RuntimeException("League is full!");
        }

        if (league.getCreatorId() != null && league.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("You cannot join a league that you created.");
        }

        String username = request.username() != null ? request.username() : "Player_" + userId;

        emailService.sendAdminJoinNotification(
                username,
                league.getName(),
                league.getJoinAmount().toString(),
                request.accountCode().trim()
        );

        return mapToResponse(league);
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

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> getActiveLeagues() {
        return leagueRepository.findByStatusInOrderByCreatedAtDesc(List.of(LeagueStatus.ACTIVE, LeagueStatus.ONGOING))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingAdminRequestDto> getPendingAdminRequests() {
        List<League> pendingLeagues = leagueRepository.findByStatusOrderByCreatedAtDesc(LeagueStatus.PENDING_APPROVAL);

        return pendingLeagues.stream().map(league -> {
            String initial = (league.getCreatorUsername() != null && !league.getCreatorUsername().isEmpty())
                    ? String.valueOf(league.getCreatorUsername().charAt(0)).toUpperCase()
                    : "U";

            return new PendingAdminRequestDto(
                    "req_" + league.getId(),
                    "CREATE",
                    initial,
                    league.getCreatorUsername(),
                    league.getName(),
                    league.getJoinAmount()
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeagueResponse approveLeague(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + id));

        league.setStatus(LeagueStatus.ACTIVE);
        League approvedLeague = leagueRepository.save(league);

        notificationService.sendApprovalNotification(
                approvedLeague.getCreatorId(),
                approvedLeague.getCreatorUsername(),
                approvedLeague.getName(),
                approvedLeague.getJoinAmount(),
                true
        );

        return mapToResponse(approvedLeague);
    }

    @Override
    @Transactional
    public void rejectLeague(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + id));

        league.setStatus(LeagueStatus.REJECTED);
        leagueRepository.save(league);

        notificationService.sendRejectionNotification(
                league.getCreatorId(),
                league.getCreatorUsername(),
                league.getName(),
                league.getJoinAmount(),
                true
        );
    }

    @Override
    @Transactional
    public LeagueResponse approveJoinRequest(Long joinRequestId) {
        League league = leagueRepository.findById(joinRequestId)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + joinRequestId));

        if (league.getCurrentTeams() < league.getMaxTeams()) {
            league.setCurrentTeams(league.getCurrentTeams() + 1);
            if (league.getCurrentTeams().equals(league.getMaxTeams())) {
                league.setStatus(LeagueStatus.ONGOING);
            }
            leagueRepository.save(league);
        }

         notificationService.sendApprovalNotification(
                league.getCreatorId(),
                league.getCreatorUsername(),
                league.getName(),
                league.getJoinAmount(),
                false
        );

        return mapToResponse(league);
    }

    @Override
    @Transactional
    public void rejectJoinRequest(Long joinRequestId) {
        League league = leagueRepository.findById(joinRequestId)
                .orElseThrow(() -> new RuntimeException("League not found with ID: " + joinRequestId));

        notificationService.sendRejectionNotification(
                league.getCreatorId(),
                league.getCreatorUsername(),
                league.getName(),
                league.getJoinAmount(),
                false // isCreation = false
        );
    }

    private LeagueResponse mapToResponse(League league) {
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getAccountCode(),
                league.getCreatorId(),
                league.getCreatorUsername(),
                league.getJoinAmount(),
                league.getMaxTeams(),
                league.getCurrentTeams(),
                league.getAwardType() != null ? AwardType.valueOf(league.getAwardType().name()) : null,
                league.getStatus() != null ? league.getStatus().name() : LeagueStatus.PENDING_APPROVAL.name(),
                league.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboardData() {
        List<PendingAdminRequestDto> pendingRequests = getPendingAdminRequests();
        List<PendingPayoutDto> pendingPayouts = List.of();

        List<LeagueStatus> historyStatuses = List.of(
                LeagueStatus.ACTIVE,
                LeagueStatus.REJECTED,
                LeagueStatus.ONGOING
        );

        List<League> historyLeagues = leagueRepository.findByStatusInOrderByCreatedAtDesc(historyStatuses);

        List<HistoryLogDto> historyLogs = historyLeagues.stream().map(league -> {
            boolean isApproved = league.getStatus() == LeagueStatus.ACTIVE || league.getStatus() == LeagueStatus.ONGOING;
            String actionType = isApproved ? "ACCEPTED_JOIN" : "REJECTED_JOIN";
            String statusText = isApproved ? "You Accepted" : "You Rejected";

            return new HistoryLogDto(
                    actionType,
                    "Recently",
                    statusText + " " + league.getCreatorUsername() + " -> " + league.getName()
            );
        }).collect(Collectors.toList());

        return new AdminDashboardDto(pendingRequests, pendingPayouts, historyLogs);
    }
}