ALTER TABLE submissions
    ADD COLUMN failed_test_case VARCHAR(255),
    ADD COLUMN failed_output TEXT;