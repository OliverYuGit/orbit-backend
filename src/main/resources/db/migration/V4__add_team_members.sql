-- V4__add_team_members.sql
-- Add team member accounts
-- Password for all: Orbit2026
-- BCrypt hash: $2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri

INSERT INTO users (username, password_hash, display_name, is_active, created_at, updated_at)
VALUES 
    ('max', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Max', 1, NOW(3), NOW(3)),
    ('luna', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Luna', 1, NOW(3), NOW(3)),
    ('bob', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Bob', 1, NOW(3), NOW(3)),
    ('sam', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Sam', 1, NOW(3), NOW(3)),
    ('alex', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Alex', 1, NOW(3), NOW(3)),
    ('mia', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Mia', 1, NOW(3), NOW(3)),
    ('nova', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Nova', 1, NOW(3), NOW(3)),
    ('kai', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Kai', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE updated_at = updated_at;
