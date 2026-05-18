-- ============================================================
-- Cart migration — 2026-05-17
-- Add cartStartDate column + update unique key
-- One row per item (was one row per type)
-- ============================================================

ALTER TABLE cart
    ADD COLUMN cartStartDate DATETIME NOT NULL DEFAULT '2000-01-01 00:00:00'
        AFTER cartClubId;

ALTER TABLE cart
    DROP INDEX ukCartPlayerClubType;

ALTER TABLE cart
    ADD UNIQUE KEY ukCartPlayerClubTypeDate (cartPlayerId, cartClubId, cartType, cartStartDate);

-- After running this migration, clear all existing PENDING cart rows
-- (they predate cartStartDate and are structurally incompatible with the new format).
DELETE FROM cart WHERE cartStatus = 'PENDING';
