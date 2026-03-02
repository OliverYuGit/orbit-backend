package com.orbit.mission.project;

import com.orbit.mission.user.UserSummaryDto;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private UserSummaryDto owner;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Instant createdAt;
    private Instant updatedAt;

    public ProjectDto(ProjectEntity entity, UserSummaryDto owner) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.status = entity.getStatus();
        this.owner = owner;
        this.startDate = entity.getStartDate();
        this.targetDate = entity.getTargetDate();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }
}
