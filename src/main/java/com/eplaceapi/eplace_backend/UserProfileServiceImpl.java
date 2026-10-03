package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(user));

        int friendCount = friendshipRepository.countAcceptedFriends(userId);

        int played = profile.getLeaguesPlayed();
        int won = profile.getLeaguesWon();
        int neverWon = Math.max(0, played - won);

        double winPct = played > 0 ? ((double) won / played) * 100.0 : 0.0;
        double lossPct = played > 0 ? 100.0 - winPct : 0.0;

        return UserProfileResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(profile.getBio())
                .location(profile.getLocation())
                .efootballStrength(profile.getEfootballStrength())
                .friendCount(friendCount)
                .createdLeaguesLimit(profile.getCreatedLeaguesLimit())
                .createdLeaguesCount(profile.getCreatedLeaguesCount())
                .leaguesPlayed(played)
                .leaguesWon(won)
                .leaguesNeverWon(neverWon)
                .winProbabilityPercentage(Math.round(winPct * 10.0) / 10.0)
                .lossProbabilityPercentage(Math.round(lossPct * 10.0) / 10.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponseDTO> searchUsersByUsername(String username) {
        List<User> matchingUsers = userRepository.findByUsernameContainingIgnoreCase(username);

        return matchingUsers.stream()
                .map(user -> getUserProfile(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateProfile(Long userId, UpdateProfileRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(user));

        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getEfootballStrength() != null) profile.setEfootballStrength(request.getEfootballStrength());

        userProfileRepository.save(profile);
        return getUserProfile(userId);
    }

    private UserProfile createDefaultProfile(User user) {
        UserProfile newProfile = UserProfile.builder()
                .user(user)
                .bio("ePlace Competitor")
                .location("Nairobi, Kenya")
                .efootballStrength(3000)
                .createdLeaguesLimit(50)
                .createdLeaguesCount(0)
                .leaguesPlayed(0)
                .leaguesWon(0)
                .build();
        return userProfileRepository.save(newProfile);
    }
}
