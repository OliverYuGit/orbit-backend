package com.orbit.mission.comment;

import lombok.Getter;

import java.time.Instant;

@Getter
public class CommentDto {
    private final Long id;
    private final Long taskId;
    private final Long authorId;
    private final String content;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CommentDto(CommentEntity c) {
        this.id = c.getId();
        this.taskId = c.getTaskId();
        this.authorId = c.getAuthorId();
        this.content = c.getContent();
        this.createdAt = c.getCreatedAt();
        this.updatedAt = c.getUpdatedAt();
    }
}
