-- Keep the file-import byte limit consistent for direct JSON writes too.
ALTER TABLE test_cases
    DROP CONSTRAINT ck_test_cases_input_length,
    DROP CONSTRAINT ck_test_cases_output_length;

ALTER TABLE test_cases
    ADD CONSTRAINT ck_test_cases_input_length CHECK (octet_length(input) <= 5242880),
    ADD CONSTRAINT ck_test_cases_output_length CHECK (octet_length(expected_output) <= 5242880);
