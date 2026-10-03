package com.eplaceapi.eplace_backend;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipOverviewDTO {
    private List<FriendDTO> friends;
    private List<FriendDTO> pendingSent;
    private List<FriendDTO> receivedRequests;
    private List<FriendDTO> suggestions;
}