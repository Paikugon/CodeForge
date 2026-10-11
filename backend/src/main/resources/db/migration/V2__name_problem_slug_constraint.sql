DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'problems'::regclass
          AND conname = 'problems_slug_key'
    ) THEN
        ALTER TABLE problems
        RENAME CONSTRAINT problems_slug_key TO uk_problems_slug;
    END IF;
END
$$;