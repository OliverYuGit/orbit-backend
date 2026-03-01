package com.orbit.mission.comment;

import com.orbit.mission.activity.ActivityService;
import com.orbit.mission.activity.ActivityType;
import com.orbit.mission.user.UserRepository;
import com.orbit.mission.user.UserSummaryDto;
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
    private final UserRepository userRepository;

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
        return toDto(comment);
    }

    public List<CommentDto> list(Long taskId) {
        return commentRepository.findByTaskIdAndDeletedAtIsNullOrderByCreatedAtAsc(taskId)
                .stream().map(this::toDto).toList();
    }

    private CommentDto toDto(CommentEntity c) {
        UserSummaryDto author = userRepository.findById(c.getAuthorId())
                .map(UserSummaryDto::new).orElse(null);
        return new CommentDto(c, author);
    }
}
