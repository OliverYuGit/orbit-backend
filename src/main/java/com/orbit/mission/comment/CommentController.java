package com.orbit.mission.comment;

import com.orbit.mission.auth.UserPrincipal;
import com.orbit.mission.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.create(taskId, req.getContent(), principal.getId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentDto>>> list(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.list(taskId)));
    }

    @Getter @Setter
    public static class CommentRequest {
        @NotBlank private String content;
    }
}
