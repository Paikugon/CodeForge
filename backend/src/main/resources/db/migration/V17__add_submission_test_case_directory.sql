ALTER TABLE submissions
    ADD COLUMN failed_input TEXT,
    ADD COLUMN failed_expected_output TEXT,
    ADD COLUMN test_case_directory_id VARCHAR(255);