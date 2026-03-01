package com.orbit.mission.activity;

import com.orbit.mission.user.UserSummaryDto;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class ActivityDto {
    private final Long id;
    private final Long taskId;
    private final UserSummaryDto actor;
    private final String type;
    private final String summary;
    private final Map<String, Object> payload;
    private final Instant createdAt;

    public ActivityDto(ActivityEntity a, UserSummaryDto actor) {
        this.id = a.getId();
        this.taskId = a.getTaskId();
        this.actor = actor;
        this.type = a.getType().name();
        this.summary = a.getSummary();
        this.payload = a.getPayload();
        this.createdAt = a.getCreatedAt();
    }
}
