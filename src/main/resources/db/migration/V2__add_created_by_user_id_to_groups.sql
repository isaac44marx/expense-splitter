ALTER TABLE groups
    ADD COLUMN created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT;

CREATE INDEX idx_groups_created_by_user_id ON groups(created_by_user_id);
