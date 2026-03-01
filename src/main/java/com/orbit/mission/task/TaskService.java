package com.orbit.mission.task;

import com.orbit.mission.activity.ActivityService;
import com.orbit.mission.activity.ActivityType;
import com.orbit.mission.common.PageResponse;
import com.orbit.mission.common.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ActivityService activityService;

    @Transactional
    public TaskDto create(TaskCreateRequest req, Long actorId) {
        TaskEntity task = new TaskEntity();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setStatus(TaskStatus.BACKLOG);
        task.setPriority(req.getPriority() != null ? req.getPriority() : TaskPriority.P1);
        task.setAssigneeId(req.getAssigneeId());
        task.setCreatedById(actorId);
        task.setSource(req.getSource());
        task.setTags(req.getTags());
        task.setDueAt(req.getDueAt());
        task = taskRepository.save(task);

        activityService.record(task.getId(), actorId, ActivityType.TASK_CREATED,
                "Task created: " + task.getTitle(), null);
        return new TaskDto(task);
    }

    public TaskDto get(Long id) {
        return new TaskDto(findActive(id));
    }

    @Transactional
    public TaskDto update(Long id, TaskUpdateRequest req, Long actorId) {
        TaskEntity task = findActive(id);
        List<String> changes = new ArrayList<>();

        if (req.getTitle() != null && !req.getTitle().equals(task.getTitle())) {
            changes.add("title");
            task.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) { task.setDescription(req.getDescription()); changes.add("description"); }
        if (req.getPriority() != null && req.getPriority() != task.getPriority()) {
            changes.add("priority:" + task.getPriority() + "->" + req.getPriority());
            task.setPriority(req.getPriority());
        }
        if (req.getAssigneeId() != null && !req.getAssigneeId().equals(task.getAssigneeId())) {
            activityService.record(task.getId(), actorId, ActivityType.TASK_ASSIGNEE_CHANGED,
                    "Assignee changed",
                    Map.of("from", task.getAssigneeId() != null ? task.getAssigneeId() : "null",
                           "to", req.getAssigneeId()));
            task.setAssigneeId(req.getAssigneeId());
        }
        if (req.getSource() != null) { task.setSource(req.getSource()); }
        if (req.getTags() != null) { task.setTags(req.getTags()); changes.add("tags"); }
        if (req.getDueAt() != null) { task.setDueAt(req.getDueAt()); }

        task = taskRepository.save(task);
        if (!changes.isEmpty()) {
            activityService.record(task.getId(), actorId, ActivityType.TASK_UPDATED,
                    "Updated: " + String.join(", ", changes), null);
        }
        return new TaskDto(task);
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        TaskEntity task = findActive(id);
        task.setDeletedAt(Instant.now());
        taskRepository.save(task);
        activityService.record(id, actorId, ActivityType.TASK_UPDATED, "Task deleted", null);
    }

    @Transactional
    public TaskDto transition(Long id, TaskStatus toStatus, Long actorId) {
        TaskEntity task = findActive(id);
        TaskStatus from = task.getStatus();
        task.setStatus(toStatus);
        task = taskRepository.save(task);
        activityService.record(id, actorId, ActivityType.TASK_STATUS_CHANGED,
                "Status: " + from + " → " + toStatus,
                Map.of("from", from.name(), "to", toStatus.name()));
        return new TaskDto(task);
    }

    @Transactional
    public TaskDto assign(Long id, Long assigneeId, Long actorId) {
        TaskEntity task = findActive(id);
        Long oldAssignee = task.getAssigneeId();
        task.setAssigneeId(assigneeId);
        task = taskRepository.save(task);
        activityService.record(id, actorId, ActivityType.TASK_ASSIGNEE_CHANGED,
                "Assignee changed",
                Map.of("from", oldAssignee != null ? oldAssignee : "null",
                       "to", assigneeId != null ? assigneeId : "null"));
        return new TaskDto(task);
    }

    public PageResponse<TaskDto> list(TaskFilter filter) {
        Specification<TaskEntity> spec = buildSpec(filter);
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        Page<TaskEntity> page = taskRepository.findAll(spec,
                PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort));
        return new PageResponse<>(page.getContent().stream().map(TaskDto::new).toList(),
                page.getTotalElements(), filter.getPage(), filter.getPageSize());
    }

    private Specification<TaskEntity> buildSpec(TaskFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (f.getStatus() != null && !f.getStatus().isEmpty()) {
                predicates.add(root.get("status").in(f.getStatus()));
            }
            if (f.getAssigneeId() != null) {
                predicates.add(cb.equal(root.get("assigneeId"), f.getAssigneeId()));
            }
            if (f.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), f.getPriority()));
            }
            if (f.getQ() != null && !f.getQ().isBlank()) {
                String like = "%" + f.getQ().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private TaskEntity findActive(Long id) {
        return taskRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }
}
