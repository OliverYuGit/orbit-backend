-- V3__add_test_user.sql
-- Add Oliver test user for Mia's testing
-- Username: Oliver
-- Password: Orbit2026

INSERT INTO users (username, password_hash, display_name, is_active, created_at, updated_at)
VALUES ('Oliver', '$2b$10$QmzwdoMVkMDsVQJrqj.KUenl.Q1wZG2asD5DaOWcbyyeGbX5ugSri', 'Oliver', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE updated_at = updated_at;
