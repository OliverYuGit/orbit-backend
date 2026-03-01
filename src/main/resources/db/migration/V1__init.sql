-- V1__init.sql
-- Orbit Mission MVP - Initial Schema
-- Created: 2026-03-01

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username        VARCHAR(64)     NOT NULL,
    password_hash   VARCHAR(100)    NOT NULL,
    display_name    VARCHAR(64)     NULL,
    avatar_url      VARCHAR(255)    NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    last_login_at   DATETIME(3)     NULL,
    created_at      DATETIME(3)     NOT NULL,
    updated_at      DATETIME(3)     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    KEY idx_users_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tasks (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT            NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT 'BACKLOG',
    priority        VARCHAR(8)      NOT NULL DEFAULT 'P1',
    assignee_id     BIGINT UNSIGNED NULL,
    created_by_id   BIGINT UNSIGNED NOT NULL,
    source          VARCHAR(64)     NULL,
    tags_json       JSON            NULL,
    due_at          DATETIME(3)     NULL,
    deleted_at      DATETIME(3)     NULL,
    created_at      DATETIME(3)     NOT NULL,
    updated_at      DATETIME(3)     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_tasks_status_updated   (status, updated_at DESC),
    KEY idx_tasks_assignee_status  (assignee_id, status, updated_at DESC),
    KEY idx_tasks_priority_status  (priority, status, updated_at DESC),
    KEY idx_tasks_created_by       (created_by_id, created_at DESC),
    KEY idx_tasks_deleted          (deleted_at),
    KEY idx_tasks_updated          (updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comments (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    task_id     BIGINT UNSIGNED NOT NULL,
    author_id   BIGINT UNSIGNED NOT NULL,
    content     TEXT            NOT NULL,
    deleted_at  DATETIME(3)     NULL,
    created_at  DATETIME(3)     NOT NULL,
    updated_at  DATETIME(3)     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_comments_task_created (task_id, created_at ASC),
    KEY idx_comments_author       (author_id, created_at DESC),
    KEY idx_comments_deleted      (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activities (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    task_id      BIGINT UNSIGNED NULL,
    actor_id     BIGINT UNSIGNED NOT NULL,
    type         VARCHAR(32)     NOT NULL,
    summary      VARCHAR(255)    NULL,
    payload_json JSON            NULL,
    created_at   DATETIME(3)     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_activities_task_created (task_id, created_at DESC),
    KEY idx_activities_created      (created_at DESC),
    KEY idx_activities_actor        (actor_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed: dev admin user (password: admin123, bcrypt strength 10)
INSERT INTO users (username, password_hash, display_name, is_active, created_at, updated_at)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE updated_at = updated_at;
