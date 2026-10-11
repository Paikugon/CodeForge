ALTER TABLE problems
ADD CONSTRAINT chk_problem_slug_format
CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$');