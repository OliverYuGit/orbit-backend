package com.orbit.mission.project;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectFilter {
    private List<ProjectStatus> status;
    private Long ownerId;
    private String q;
    private int page = 1;
    private int pageSize = 20;
}
