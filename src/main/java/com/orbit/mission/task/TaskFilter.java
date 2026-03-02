package com.orbit.mission.task;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class TaskFilter {
    private List<TaskStatus> status;
    private Long assigneeId;
    private Long projectId;
    private TaskPriority priority;
    private String tag;
    private String q;
    private int page = 1;
    private int pageSize = 20;
}
