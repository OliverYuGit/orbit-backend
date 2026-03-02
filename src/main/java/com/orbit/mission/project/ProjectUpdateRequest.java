package com.orbit.mission.project;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectUpdateRequest {
    private String name;
    private String description;
    private ProjectStatus status;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate targetDate;
}
