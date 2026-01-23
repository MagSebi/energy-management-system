-- Test PostgreSQL connectivity and verify monitoring database setup
-- Run this script with: psql -h localhost -p 5435 -U postgres -d monitoring-db -f test-postgres.sql

\echo '==================================='
\echo 'Monitoring Database - PostgreSQL Test'
\echo '==================================='
\echo ''

-- 1. Show PostgreSQL version
\echo 'PostgreSQL Version:'
SELECT version();
\echo ''

-- 2. Show current database
\echo 'Current Database:'
SELECT current_database();
\echo ''

-- 3. Show current user
\echo 'Current User:'
SELECT current_user;
\echo ''

-- 4. List all tables
\echo 'Tables in monitoring-db:'
SELECT
    schemaname,
    tablename,
    tableowner
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;
\echo ''

-- 5. Check if our tables exist
\echo 'Checking for required tables...'
SELECT
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'device_measurements')
        THEN '✓ device_measurements exists'
        ELSE '✗ device_measurements NOT FOUND'
    END as table_check
UNION ALL
SELECT
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'hourly_energy_consumption')
        THEN '✓ hourly_energy_consumption exists'
        ELSE '✗ hourly_energy_consumption NOT FOUND'
    END;
\echo ''

-- 6. Show table structures (if they exist)
\echo 'Table Structure - device_measurements:'
\d device_measurements
\echo ''

\echo 'Table Structure - hourly_energy_consumption:'
\d hourly_energy_consumption
\echo ''

-- 7. Count records
\echo 'Record Counts:'
SELECT
    'device_measurements' as table_name,
    COUNT(*) as record_count
FROM device_measurements
UNION ALL
SELECT
    'hourly_energy_consumption',
    COUNT(*)
FROM hourly_energy_consumption;
\echo ''

-- 8. Show sample data (if any)
\echo 'Sample Device Measurements (latest 5):'
SELECT
    id,
    device_id,
    TO_CHAR(timestamp, 'YYYY-MM-DD HH24:MI:SS') as timestamp,
    measurement_value
FROM device_measurements
ORDER BY timestamp DESC
LIMIT 5;
\echo ''

\echo 'Sample Hourly Consumption (latest 5):'
SELECT
    id,
    device_id,
    TO_CHAR(hour_timestamp, 'YYYY-MM-DD HH24:00') as hour,
    ROUND(total_consumption::numeric, 2) as total_consumption
FROM hourly_energy_consumption
ORDER BY hour_timestamp DESC
LIMIT 5;
\echo ''

-- 9. Check indexes
\echo 'Indexes:'
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
\echo ''

\echo '==================================='
\echo 'Test Complete!'
\echo '==================================='

