package com.orbit.mission.comment;

import com.orbit.mission.activity.ActivityService;
import com.orbit.mission.activity.ActivityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ActivityService activityService;

    @Transactional
    public CommentDto create(Long taskId, String content, Long authorId) {
        CommentEntity comment = new CommentEntity();
        comment.setTaskId(taskId);
        comment.setAuthorId(authorId);
        comment.setContent(content);
        comment = commentRepository.save(comment);

        activityService.record(taskId, authorId, ActivityType.COMMENT_CREATED,
                "Comment added",
                Map.of("commentId", comment.getId()));
        return new CommentDto(comment);
    }

    public List<CommentDto> list(Long taskId) {
        return commentRepository.findByTaskIdAndDeletedAtIsNullOrderByCreatedAtAsc(taskId)
                .stream().map(CommentDto::new).toList();
    }
}
