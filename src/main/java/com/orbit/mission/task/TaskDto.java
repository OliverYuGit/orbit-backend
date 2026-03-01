package com.orbit.mission.task;

import com.orbit.mission.user.UserSummaryDto;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class TaskDto {
    private final Long id;
    private final String title;
    private final String description;
    private final String status;
    private final String priority;
    private final Long assigneeId;
    private final UserSummaryDto assignee;
    private final Long createdById;
    private final UserSummaryDto createdBy;
    private final String source;
    private final List<String> tags;
    private final Instant dueAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    public TaskDto(TaskEntity t, UserSummaryDto assignee, UserSummaryDto createdBy) {
        this.id = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.status = t.getStatus().name();
        this.priority = t.getPriority().name();
        this.assigneeId = t.getAssigneeId();
        this.assignee = assignee;
        this.createdById = t.getCreatedById();
        this.createdBy = createdBy;
        this.source = t.getSource();
        this.tags = t.getTags();
        this.dueAt = t.getDueAt();
        this.createdAt = t.getCreatedAt();
        this.updatedAt = t.getUpdatedAt();
    }
}
