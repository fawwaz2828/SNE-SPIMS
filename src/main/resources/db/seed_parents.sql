-- ============================================================================
-- SNE-SPIMS — parent top-up for the extra students (students 4..15)
--
-- Gives each student who had no parent a linked parent account (password:
-- parent123), so teacher/parent chat rooms have real contacts to message.
-- Idempotent: INSERT IGNORE skips users/links that already exist.
--
--   mysql>  USE sne_spims;
--   mysql>  SOURCE src/main/resources/db/seed_parents.sql;
-- ============================================================================
USE sne_spims;

-- All parents share the demo password "parent123" (same BCrypt hash as robert/linda).
INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, active) VALUES
 ('sarah.brooks', '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Sarah Brooks', 'sarah.brooks@sne.test','0811000005','PARENT',TRUE),
 ('paul.bennett', '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Paul Bennett', 'paul.bennett@sne.test','0811000006','PARENT',TRUE),
 ('emma.carter',  '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Emma Carter',  'emma.carter@sne.test','0811000007','PARENT',TRUE),
 ('david.hayes',  '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','David Hayes',  'david.hayes@sne.test','0811000008','PARENT',TRUE),
 ('karen.foster', '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Karen Foster', 'karen.foster@sne.test','0811000009','PARENT',TRUE),
 ('mark.reed',    '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Mark Reed',    'mark.reed@sne.test','0811000010','PARENT',TRUE),
 ('anna.gray',    '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Anna Gray',    'anna.gray@sne.test','0811000011','PARENT',TRUE),
 ('john.price',   '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','John Price',   'john.price@sne.test','0811000012','PARENT',TRUE),
 ('helen.ward',   '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Helen Ward',   'helen.ward@sne.test','0811000013','PARENT',TRUE),
 ('peter.hughes', '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Peter Hughes', 'peter.hughes@sne.test','0811000014','PARENT',TRUE),
 ('mary.bell',    '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Mary Bell',    'mary.bell@sne.test','0811000015','PARENT',TRUE),
 ('tom.morgan',   '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Tom Morgan',   'tom.morgan@sne.test','0811000016','PARENT',TRUE);

-- Link each parent to their child (by username + student id). INSERT IGNORE keeps it re-runnable.
INSERT IGNORE INTO parent_student (parent_id, student_id, relationship)
SELECT u.id, x.sid, x.rel FROM (
    SELECT 'sarah.brooks' AS uname,  4 AS sid, 'MOTHER' AS rel UNION ALL
    SELECT 'paul.bennett',  5, 'FATHER'  UNION ALL
    SELECT 'emma.carter',   6, 'MOTHER'  UNION ALL
    SELECT 'david.hayes',   7, 'FATHER'  UNION ALL
    SELECT 'karen.foster',  8, 'MOTHER'  UNION ALL
    SELECT 'mark.reed',     9, 'FATHER'  UNION ALL
    SELECT 'anna.gray',    10, 'MOTHER'  UNION ALL
    SELECT 'john.price',   11, 'FATHER'  UNION ALL
    SELECT 'helen.ward',   12, 'MOTHER'  UNION ALL
    SELECT 'peter.hughes', 13, 'FATHER'  UNION ALL
    SELECT 'mary.bell',    14, 'MOTHER'  UNION ALL
    SELECT 'tom.morgan',   15, 'FATHER'
) x JOIN users u ON u.username = x.uname;
