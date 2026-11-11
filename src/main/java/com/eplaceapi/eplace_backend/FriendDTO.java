package com.eplaceapi.eplace_backend;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String location;
    private String timeAgo;
    private String profilePictureUrl;
    private Integer efootballStrength;
    private Boolean isOnline;
}