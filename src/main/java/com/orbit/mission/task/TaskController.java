package com.orbit.mission.task;

import com.orbit.mission.auth.UserPrincipal;
import com.orbit.mission.common.ApiResponse;
import com.orbit.mission.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDto>> create(
            @Valid @RequestBody TaskCreateRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(taskService.create(req, principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> update(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.update(id, req, principal.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskDto>>> list(
            @RequestParam(required = false) List<TaskStatus> status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        TaskFilter filter = new TaskFilter();
        filter.setStatus(status);
        filter.setAssigneeId(assigneeId);
        filter.setProjectId(projectId);
        filter.setPriority(priority);
        filter.setTag(tag);
        filter.setQ(q);
        filter.setPage(page);
        filter.setPageSize(pageSize);
        return ResponseEntity.ok(ApiResponse.ok(taskService.list(filter)));
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<ApiResponse<TaskDto>> transition(
            @PathVariable Long id,
            @Valid @RequestBody TransitionRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.transition(id, req.getToStatus(), principal.getId())));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TaskDto>> assign(
            @PathVariable Long id,
            @RequestBody AssignRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.assign(id, req.getAssigneeId(), principal.getId())));
    }

    @Getter @Setter
    public static class TransitionRequest {
        @NotNull private TaskStatus toStatus;
    }

    @Getter @Setter
    public static class AssignRequest {
        private Long assigneeId; // null = unassign
    }
}
