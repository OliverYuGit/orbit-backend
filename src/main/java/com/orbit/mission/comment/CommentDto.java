package com.orbit.mission.comment;

import com.orbit.mission.user.UserSummaryDto;
import lombok.Getter;

import java.time.Instant;

@Getter
public class CommentDto {
    private final Long id;
    private final Long taskId;
    private final String content;
    private final UserSummaryDto author;
    private final Instant createdAt;

    public CommentDto(CommentEntity c, UserSummaryDto author) {
        this.id = c.getId();
        this.taskId = c.getTaskId();
        this.content = c.getContent();
        this.author = author;
        this.createdAt = c.getCreatedAt();
    }
}
