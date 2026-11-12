-- Keep V1-V13 unchanged so existing Flyway history remains valid.
-- Existing invalid rows must be corrected before upgrading an existing database.
ALTER TABLE problems DROP CONSTRAINT ck_problems_spj_source;

ALTER TABLE problems
    ADD CONSTRAINT ck_problems_title_not_blank CHECK (title ~ '[^[:space:]]'),
    ADD CONSTRAINT ck_problems_description_not_blank CHECK (description ~ '[^[:space:]]'),
    ADD CONSTRAINT ck_problems_description_length CHECK (char_length(description) <= 100000),
    ADD CONSTRAINT ck_problems_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    ADD CONSTRAINT ck_problems_time_limit_max CHECK (time_limit_ms <= 30000),
    ADD CONSTRAINT ck_problems_memory_limit_max CHECK (memory_limit_mb <= 1024),
    ADD CONSTRAINT ck_problems_spj_source_length CHECK (char_length(spj_source) <= 65536),
    ADD CONSTRAINT ck_problems_spj_source CHECK (
        NOT spj_enabled OR (spj_source IS NOT NULL AND spj_source ~ '[^[:space:]]')
    );

-- Empty input/output are valid for some problems; NULL is already forbidden by V4.
ALTER TABLE test_cases
    ADD CONSTRAINT ck_test_cases_input_length CHECK (char_length(input) <= 1000000),
    ADD CONSTRAINT ck_test_cases_output_length CHECK (char_length(expected_output) <= 1000000);

-- These entities read generated timestamps from the database.
CREATE FUNCTION codeforge_touch_updated_at() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_problems_updated_at
    BEFORE UPDATE ON problems
    FOR EACH ROW EXECUTE FUNCTION codeforge_touch_updated_at();

CREATE TRIGGER trg_test_cases_updated_at
    BEFORE UPDATE ON test_cases
    FOR EACH ROW EXECUTE FUNCTION codeforge_touch_updated_at();
