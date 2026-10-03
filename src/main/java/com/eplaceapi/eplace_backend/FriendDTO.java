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
    private String timeAgo;
    private Boolean isOnline;
}