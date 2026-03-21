-- V26: Clean all device catalog data for fresh seed
-- Order matters: models first (FK to category+brand), then mapping, then brands, then categories

TRUNCATE TABLE device_model CASCADE;
TRUNCATE TABLE category_brand CASCADE;
TRUNCATE TABLE device_brand CASCADE;
TRUNCATE TABLE device_category CASCADE;
