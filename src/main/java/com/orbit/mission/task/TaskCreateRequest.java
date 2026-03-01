package com.orbit.mission.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class TaskCreateRequest {
    @NotBlank private String title;
    private String description;
    private TaskPriority priority;
    private Long assigneeId;
    private String source;
    private List<String> tags;
    private Instant dueAt;
}
