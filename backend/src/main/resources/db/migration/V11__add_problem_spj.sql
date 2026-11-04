ALTER TABLE problems
    ADD COLUMN spj_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN spj_source TEXT;

ALTER TABLE problems
    ADD CONSTRAINT ck_problems_spj_source
    CHECK (
        NOT spj_enabled
        OR (
            spj_source IS NOT NULL
            AND length(trim(spj_source)) > 0
        )
    );