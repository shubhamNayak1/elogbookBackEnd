ALTER TABLE users
  ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Force the seeded admin to rotate the well-known default on first login.
UPDATE users SET must_change_password = TRUE WHERE username = 'admin';
