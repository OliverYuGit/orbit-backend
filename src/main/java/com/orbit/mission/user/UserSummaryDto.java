package com.orbit.mission.user;

import lombok.Getter;

@Getter
public class UserSummaryDto {
    private final Long id;
    private final String username;
    private final String displayName;
    private final String avatarUrl;

    public UserSummaryDto(UserEntity u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.displayName = u.getDisplayName();
        this.avatarUrl = u.getAvatarUrl();
    }
}
