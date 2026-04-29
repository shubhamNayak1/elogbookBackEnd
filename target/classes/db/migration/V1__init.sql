CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
  id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  username        VARCHAR(50) UNIQUE NOT NULL,
  full_name       VARCHAR(255) NOT NULL,
  role            VARCHAR(10) NOT NULL CHECK (role IN ('ADMIN','USER')),
  status          VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
  password_hash   VARCHAR(255),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE logbook_templates (
  id              VARCHAR(50) PRIMARY KEY,
  name            VARCHAR(255) NOT NULL,
  description     TEXT,
  status          VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('DRAFT','ACTIVE','INACTIVE')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      VARCHAR(50) NOT NULL REFERENCES users(username)
);

CREATE TABLE logbook_columns (
  id                 VARCHAR(100) PRIMARY KEY,
  logbook_id         VARCHAR(50) NOT NULL REFERENCES logbook_templates(id) ON DELETE CASCADE,
  label              VARCHAR(255) NOT NULL,
  key                VARCHAR(100) NOT NULL,
  type               VARCHAR(20) NOT NULL CHECK (type IN ('TEXT','NUMBER','DATE','TIME','DATETIME','DROPDOWN','BOOLEAN')),
  is_mandatory       BOOLEAN NOT NULL DEFAULT FALSE,
  is_system_managed  BOOLEAN NOT NULL DEFAULT FALSE,
  options            JSONB,
  display_order      INT NOT NULL,
  group_name         VARCHAR(100),
  UNIQUE (logbook_id, key)
);
CREATE INDEX idx_logbook_columns_logbook ON logbook_columns(logbook_id, display_order);

CREATE TABLE logbook_entries (
  id                  VARCHAR(50) PRIMARY KEY,
  logbook_id          VARCHAR(50) NOT NULL REFERENCES logbook_templates(id),
  values              JSONB NOT NULL,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by          VARCHAR(255) NOT NULL,
  created_by_user_id  UUID NOT NULL REFERENCES users(id),
  status              VARCHAR(10) NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED','SIGNED','DELETED')),
  reason              TEXT
);
CREATE INDEX idx_entries_logbook ON logbook_entries(logbook_id, created_at);
CREATE INDEX idx_entries_user ON logbook_entries(created_by_user_id, created_at);

CREATE TABLE audit_records (
  id           VARCHAR(50) PRIMARY KEY,
  entity_type  VARCHAR(30) NOT NULL CHECK (entity_type IN ('USER','LOGBOOK_TEMPLATE','LOGBOOK_ENTRY','SYSTEM')),
  entity_id    VARCHAR(100) NOT NULL,
  action       VARCHAR(20) NOT NULL CHECK (action IN ('CREATE','UPDATE','DELETE','VIEW_REPORT','LOGIN')),
  old_value    JSONB,
  new_value    JSONB,
  user_id      UUID NOT NULL REFERENCES users(id),
  username     VARCHAR(50) NOT NULL,
  reason       TEXT NOT NULL,
  timestamp    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_user_ts ON audit_records (user_id, timestamp);
CREATE INDEX idx_audit_entity ON audit_records (entity_type, entity_id);
CREATE INDEX idx_audit_ts ON audit_records (timestamp);

-- Append-only enforcement at the DB level (defense in depth).
CREATE OR REPLACE FUNCTION audit_records_no_mutate() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'audit_records is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_records_no_update
BEFORE UPDATE ON audit_records
FOR EACH ROW EXECUTE FUNCTION audit_records_no_mutate();

CREATE TRIGGER audit_records_no_delete
BEFORE DELETE ON audit_records
FOR EACH ROW EXECUTE FUNCTION audit_records_no_mutate();

-- Seed an initial admin so the FE login screen has a target on first boot.
INSERT INTO users (id, username, full_name, role, status)
VALUES (uuid_generate_v4(), 'admin', 'Default Admin', 'ADMIN', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;
