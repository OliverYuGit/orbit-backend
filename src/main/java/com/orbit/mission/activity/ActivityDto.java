package com.orbit.mission.activity;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class ActivityDto {
    private final Long id;
    private final Long taskId;
    private final Long actorId;
    private final String type;
    private final String summary;
    private final Map<String, Object> payload;
    private final Instant createdAt;

    public ActivityDto(ActivityEntity a) {
        this.id = a.getId();
        this.taskId = a.getTaskId();
        this.actorId = a.getActorId();
        this.type = a.getType().name();
        this.summary = a.getSummary();
        this.payload = a.getPayload();
        this.createdAt = a.getCreatedAt();
    }
}
