ALTER TABLE problems
    ADD COLUMN created_by BIGINT;

ALTER TABLE problems
    ADD CONSTRAINT fk_problems_created_by
    FOREIGN KEY (created_by) REFERENCES users(id)
    ON DELETE SET NULL;

CREATE INDEX idx_problems_created_by ON problems(created_by);
