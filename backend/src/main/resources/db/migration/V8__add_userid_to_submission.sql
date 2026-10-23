ALTER TABLE submissions
ADD COLUMN user_id BIGINT;

ALTER TABLE problems
ADD COLUMN difficulty VARCHAR(20) NOT NULL DEFAULT 'EASY';

ALTER TABLE submissions
ADD CONSTRAINT fk_submissions_user
FOREIGN KEY (user_id)
REFERENCES users(id);

CREATE INDEX idx_submissions_user_created
ON submissions(user_id, created_at DESC);

-- Truy vấn các bài đã giải (Accepted)
CREATE INDEX idx_submissions_user_ac
ON submissions(user_id, problem_id)
WHERE verdict = 'AC';

CREATE INDEX idx_submissions_problem_created
ON submissions(problem_id, created_at DESC);