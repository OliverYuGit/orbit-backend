-- V5__add_projects.sql
-- Orbit Mission - Project Layer
-- Created: 2026-03-02

CREATE TABLE IF NOT EXISTS projects (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT            NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    owner_id        BIGINT UNSIGNED NOT NULL,
    start_date      DATE            NULL,
    target_date     DATE            NULL,
    deleted_at      DATETIME(3)     NULL,
    created_at      DATETIME(3)     NOT NULL,
    updated_at      DATETIME(3)     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_projects_status_updated (status, updated_at DESC),
    KEY idx_projects_owner          (owner_id, created_at DESC),
    KEY idx_projects_deleted        (deleted_at),
    KEY idx_projects_updated        (updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add project_id to tasks table
ALTER TABLE tasks
ADD COLUMN project_id BIGINT UNSIGNED NULL AFTER created_by_id,
ADD KEY idx_tasks_project_status (project_id, status, updated_at DESC);
