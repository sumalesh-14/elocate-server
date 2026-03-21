-- ============================================================
-- V25: Remove redundant email and contact_number from
--      recycling_facility — these live in the user table.
-- ============================================================

-- 1. Verification: show counts before we touch anything
DO $$
DECLARE
    total_facilities      INTEGER;
    facilities_with_user  INTEGER;
    email_matches         INTEGER;
    orphan_email          INTEGER;
BEGIN
    SELECT COUNT(*)                          INTO total_facilities   FROM recycling_facility;
    SELECT COUNT(*)                          INTO facilities_with_user FROM recycling_facility WHERE user_id IS NOT NULL;
    SELECT COUNT(*)                          INTO email_matches
        FROM recycling_facility rf
        JOIN "user" u ON u.id = rf.user_id
        WHERE rf.email = u.email;
    SELECT COUNT(*)                          INTO orphan_email
        FROM recycling_facility
        WHERE email IS NOT NULL AND user_id IS NULL;

    RAISE NOTICE '=== V25 Pre-migration verification ===';
    RAISE NOTICE 'Total facilities            : %', total_facilities;
    RAISE NOTICE 'Facilities linked to a user : %', facilities_with_user;
    RAISE NOTICE 'Email matches user.email    : %', email_matches;
    RAISE NOTICE 'Facilities with email but no user (orphans): %', orphan_email;
END $$;

-- 2. Drop the redundant columns
ALTER TABLE recycling_facility DROP COLUMN IF EXISTS email;
ALTER TABLE recycling_facility DROP COLUMN IF EXISTS contact_number;

-- 3. Post-drop verification
DO $$
DECLARE
    col_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO col_count
    FROM information_schema.columns
    WHERE table_name = 'recycling_facility'
      AND column_name IN ('email', 'contact_number');

    IF col_count = 0 THEN
        RAISE NOTICE '✅ V25 complete — email and contact_number removed from recycling_facility';
    ELSE
        RAISE EXCEPTION '❌ V25 failed — % column(s) still present', col_count;
    END IF;
END $$;
