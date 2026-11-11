package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;

    private final String uploadDir = "uploads/profiles/";

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
                .profilePictureUrl(profile.getProfilePictureUrl())
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
        return updateProfile(userId, request, null);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateProfile(Long userId, UpdateProfileRequestDTO request, MultipartFile profilePicture) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new IllegalArgumentException("Username is already taken.");
                }
                user.setUsername(newUsername);
            }
        }


        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("Current password is required to set a new password.");
            }

             if (!passwordEncoder.matches(request.getCurrentPassword().trim(), user.getPassword())) {
                throw new IllegalArgumentException("Incorrect current password.");
            }


            user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }
        userRepository.save(user);


        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(user));


        if (request.getBio() != null) profile.setBio(request.getBio().trim());
        if (request.getLocation() != null) profile.setLocation(request.getLocation().trim());
        if (request.getEfootballStrength() != null) profile.setEfootballStrength(request.getEfootballStrength());

          if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String fileName = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, profilePicture.getBytes());

                profile.setProfilePictureUrl("/uploads/profiles/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store profile picture: " + e.getMessage());
            }
        }

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