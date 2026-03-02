package com.orbit.mission.project;

import com.orbit.mission.auth.UserPrincipal;
import com.orbit.mission.common.ApiResponse;
import com.orbit.mission.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDto>> create(
            @Valid @RequestBody ProjectCreateRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(projectService.create(req, principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> update(
            @PathVariable Long id,
            @RequestBody ProjectUpdateRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.update(id, req, principal.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectDto>>> list(
            @RequestParam(required = false) List<ProjectStatus> status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        ProjectFilter filter = new ProjectFilter();
        filter.setStatus(status);
        filter.setOwnerId(ownerId);
        filter.setQ(q);
        filter.setPage(page);
        filter.setPageSize(pageSize);
        return ResponseEntity.ok(ApiResponse.ok(projectService.list(filter)));
    }
}
