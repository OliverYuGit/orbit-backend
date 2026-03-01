package com.orbit.mission.user;

import lombok.Getter;

@Getter
public class UserDto {
    private final Long id;
    private final String username;
    private final String displayName;
    private final String avatarUrl;
    private final boolean active;

    public UserDto(UserEntity u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.displayName = u.getDisplayName();
        this.avatarUrl = u.getAvatarUrl();
        this.active = u.isActive();
    }
}
