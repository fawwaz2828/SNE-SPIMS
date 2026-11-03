-- ============================================================================
-- SNE-SPIMS seed data  (run AFTER schema.sql)
--
-- DEMO LOGINS
--   admin  / admin123     System Admin
--   james  / teacher123   Teacher  — Class A teacher (Ryan, Daisy, Jacob + others)
--   laura  / teacher123   Teacher  — Class B teacher
--   robert / parent123    Parent   — father of Ryan
--   linda  / parent123    Parent   — mother of Daisy, guardian of Jacob (2 children!)
--
-- THE DEMO STORY
--   Ryan  Walker (10, autism, moderate)        — clear progress: his "two-word
--         requests" milestone moved Not achieved → Emerging → Achieved.
--   Daisy Cooper (10, speech/language, low)    — working on /s/ articulation.
--   Jacob Cooper (11, intellectual, high)      — learning numbers & self-care.
--
-- All dates are relative to CURDATE()/this week's Monday, so every dashboard,
-- checklist and timetable is always populated no matter when the demo runs.
-- Only LIVE features are seeded (no attendance / medical / goal-bank rows).
-- ============================================================================
USE sne_spims;

-- ============================================================================
-- 1. ACCOUNTS
-- ============================================================================
INSERT INTO users (id, username, password_hash, full_name, email, phone, role, active) VALUES
 (1,'admin',  '$2a$10$YhcKyRmtHxHzgKQAr894a..4YcqOWD0OEOuODQUoF9yn1X0rGpeGO','System Admin','admin@sne.test','0810000001','ADMIN',TRUE),
 (2,'james',  '$2a$10$iHKxQjxaPmXgXVR1rnaex.MwzcKsEGhIWP5hKSEyUWn7rAuIC0h.q','James Wilson','james@sne.test','0810000002','TEACHER',TRUE),
 (3,'laura',  '$2a$10$iHKxQjxaPmXgXVR1rnaex.MwzcKsEGhIWP5hKSEyUWn7rAuIC0h.q','Laura Mitchell','laura@sne.test','0810000003','TEACHER',TRUE),
 (4,'robert', '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Robert Walker','robert@sne.test','0810000004','PARENT',TRUE),
 (5,'linda',  '$2a$10$ylil5fvDHAGtkpiagaWI1ePaLwyHEhgN9tQv6QU78aQuLL51kFpvW','Linda Cooper','linda@sne.test','0810000005','PARENT',TRUE);

-- ============================================================================
-- 2. STUDENTS
-- ============================================================================
INSERT INTO students (id, student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active) VALUES
 (1,'SNE-001','Ryan Walker','2016-03-12','MALE','AUTISM','MODERATE','2023-07-15','Class A','Responds well to visual schedules and predictable routines.',TRUE),
 (2,'SNE-002','Daisy Cooper','2015-09-01','FEMALE','SPEECH_LANGUAGE','LOW','2023-07-15','Class A','Confident in small groups; working on articulation.',TRUE),
 (3,'SNE-003','Jacob Cooper','2014-11-20','MALE','INTELLECTUAL','HIGH','2022-07-10','Class A','Daisy''s sibling. Learns best with concrete materials and frequent breaks.',TRUE);

-- ============================================================================
-- 3. RELATIONSHIPS  (teacher caseloads + parent links)
-- ============================================================================
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES
 (2,1,TRUE),   -- James  is Ryan's  primary teacher
 (2,2,TRUE),   -- James  is Daisy's primary teacher
 (2,3,TRUE);   -- James  is Jacob's primary teacher (Daisy's sibling, same Class A)
INSERT INTO parent_student (parent_id, student_id, relationship) VALUES
 (4,1,'FATHER'),    -- Robert → Ryan
 (5,2,'MOTHER'),    -- Linda  → Daisy
 (5,3,'GUARDIAN');  -- Linda  → Jacob  (demo: one parent, two children)

-- ============================================================================
-- 4. IEP  (one ACTIVE program per student, goals, weekly progress snapshots)
-- ============================================================================
INSERT INTO iep_programs (id, student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by) VALUES
 (1,1,'IEP Ryan 2024/2025','2024/2025', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), DATE_ADD(CURDATE(), INTERVAL 7 MONTH),
   'Ryan communicates mostly with single words and gestures. He recognises his name and follows one-step instructions with visual support. Emerging tripod pencil grasp.',
   'Visual schedule, extra processing time, seating near the teacher, movement breaks every 20 minutes.','ACTIVE',2),
 (2,2,'IEP Daisy 2024/2025','2024/2025', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), DATE_ADD(CURDATE(), INTERVAL 7 MONTH),
   'Daisy speaks in full sentences but omits the /s/ and /r/ sounds. Comprehension is age-appropriate. Confident in small groups.',
   'Speech cue cards, reduced background noise, model-and-repeat prompts.','ACTIVE',2),
 (3,3,'IEP Jacob 2024/2025','2024/2025', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), DATE_ADD(CURDATE(), INTERVAL 7 MONTH),
   'Jacob counts reliably to 7 and matches numerals 1-10 with support. Sustains attention for about 10 minutes. Benefits from concrete materials.',
   'Manipulatives for maths, shortened tasks, frequent breaks, first-then board.','ACTIVE',2);

INSERT INTO iep_goals (id, iep_id, domain, description, target_date, status, progress_pct) VALUES
 -- Ryan: 3 goals in 3 areas
 (1,1,'COMMUNICATION',   'Use 3-word phrases to request items',        DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',60),
 (2,1,'SOCIAL_EMOTIONAL','Take turns in a small-group game',           DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',45),
 (3,1,'MOTOR',           'Hold pencil with tripod grasp',              DATE_ADD(CURDATE(), INTERVAL 1 MONTH),'IN_PROGRESS',70),
 -- Daisy: 2 goals
 (4,2,'COMMUNICATION',   'Articulate /s/ sound in initial position',   DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',55),
 (5,2,'SOCIAL_EMOTIONAL','Greet peers using their name',               DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',65),
 -- Jacob: 2 goals
 (6,3,'ACADEMIC',        'Identify numbers 1-20',                      DATE_ADD(CURDATE(), INTERVAL 4 MONTH),'IN_PROGRESS',40),
 (7,3,'SELF_CARE',       'Put on shoes independently',                 DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',50);

-- Weekly snapshots (drive the "Progress trend by domain" chart, always upward)
INSERT INTO iep_goal_progress_history (goal_id, recorded_on, progress_pct, recorded_by) VALUES
 (1, DATE_SUB(CURDATE(), INTERVAL 42 DAY), 20, 2), (1, DATE_SUB(CURDATE(), INTERVAL 35 DAY), 30, 2),
 (1, DATE_SUB(CURDATE(), INTERVAL 28 DAY), 35, 2), (1, DATE_SUB(CURDATE(), INTERVAL 21 DAY), 45, 2),
 (1, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 50, 2), (1, DATE_SUB(CURDATE(), INTERVAL 7  DAY), 60, 2),
 (2, DATE_SUB(CURDATE(), INTERVAL 42 DAY), 10, 2), (2, DATE_SUB(CURDATE(), INTERVAL 28 DAY), 25, 2),
 (2, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 35, 2), (2, DATE_SUB(CURDATE(), INTERVAL 7  DAY), 45, 2),
 (3, DATE_SUB(CURDATE(), INTERVAL 42 DAY), 40, 2), (3, DATE_SUB(CURDATE(), INTERVAL 28 DAY), 55, 2),
 (3, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 65, 2), (3, DATE_SUB(CURDATE(), INTERVAL 7  DAY), 70, 2),
 (4, DATE_SUB(CURDATE(), INTERVAL 35 DAY), 30, 2), (4, DATE_SUB(CURDATE(), INTERVAL 21 DAY), 40, 2),
 (4, DATE_SUB(CURDATE(), INTERVAL 7  DAY), 55, 2),
 (5, DATE_SUB(CURDATE(), INTERVAL 28 DAY), 45, 2), (5, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 55, 2),
 (5, DATE_SUB(CURDATE(), INTERVAL 7  DAY), 65, 2),
 (6, DATE_SUB(CURDATE(), INTERVAL 35 DAY), 20, 2), (6, DATE_SUB(CURDATE(), INTERVAL 21 DAY), 30, 2),
 (6, DATE_SUB(CURDATE(), INTERVAL 7  DAY), 40, 2),
 (7, DATE_SUB(CURDATE(), INTERVAL 21 DAY), 30, 2), (7, DATE_SUB(CURDATE(), INTERVAL 7  DAY), 50, 2);

-- ============================================================================
-- 6. DAILY REPORTS  (published; each ties into the IEP story)
-- ============================================================================
INSERT INTO daily_reports (id, student_id, teacher_id, report_date, summary, mood, status, published_at) VALUES
 (1,1,2, DATE_SUB(CURDATE(), INTERVAL 1 DAY),'Ryan used a 3-word phrase to ask for the iPad today — a great milestone. Stayed regulated through transitions with his visual schedule.','HAPPY','PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
 (2,1,2, DATE_SUB(CURDATE(), INTERVAL 4 DAY),'Calm and focused morning. Worked on pencil grip; needed a few reminders but improving.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 4 DAY)),
 (3,1,2, DATE_SUB(CURDATE(), INTERVAL 8 DAY),'A tired day after the long weekend. Participated in group circle with support.','TIRED','PUBLISHED', DATE_SUB(NOW(), INTERVAL 8 DAY)),
 (4,2,2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Daisy nailed the /s/ sound in 4 of 5 words during speech practice. Very proud of herself.','HAPPY','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (5,2,2, DATE_SUB(CURDATE(), INTERVAL 6 DAY),'Greeted two classmates by name unprompted. Steady, settled day.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 6 DAY)),
 (6,3,2, DATE_SUB(CURDATE(), INTERVAL 1 DAY),'Jacob counted to 9 with the blocks today. Got anxious during the fire drill but recovered after a short break.','ANXIOUS','PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
 (7,3,2, DATE_SUB(CURDATE(), INTERVAL 5 DAY),'Put on his own shoes with only one verbal cue. Engaged well in the number game.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (1,'Academic Engagement','PROFICIENT','Focused in 1:1 work'),
 (1,'Social Interaction','DEVELOPING','Brief peer play'),
 (1,'Emotional Regulation','PROFICIENT','Smooth transitions'),
 (1,'Communication','PROFICIENT','3-word request!'),
 (2,'Academic Engagement','DEVELOPING',''),
 (2,'Social Interaction','DEVELOPING',''),
 (2,'Emotional Regulation','PROFICIENT',''),
 (2,'Communication','DEVELOPING',''),
 (3,'Academic Engagement','EMERGING','Tired'),
 (3,'Social Interaction','DEVELOPING',''),
 (3,'Emotional Regulation','DEVELOPING',''),
 (3,'Communication','EMERGING',''),
 (4,'Academic Engagement','PROFICIENT',''),
 (4,'Social Interaction','PROFICIENT',''),
 (4,'Emotional Regulation','PROFICIENT',''),
 (4,'Communication','MASTERED','/s/ in 4/5 words'),
 (5,'Academic Engagement','DEVELOPING',''),
 (5,'Social Interaction','PROFICIENT','Greeted peers'),
 (5,'Emotional Regulation','PROFICIENT',''),
 (5,'Communication','PROFICIENT',''),
 (6,'Academic Engagement','PROFICIENT','Counted to 9'),
 (6,'Social Interaction','DEVELOPING',''),
 (6,'Emotional Regulation','EMERGING','Fire-drill anxiety'),
 (6,'Communication','DEVELOPING',''),
 (7,'Academic Engagement','PROFICIENT',''),
 (7,'Social Interaction','DEVELOPING',''),
 (7,'Emotional Regulation','PROFICIENT',''),
 (7,'Communication','DEVELOPING','');

-- Parents confirmed they read the newest report
INSERT INTO report_acknowledgements (report_id, parent_id, acknowledged_at) VALUES
 (1,4, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
 (6,5, DATE_SUB(NOW(), INTERVAL 18 HOUR));

-- ============================================================================
-- 7. THERAPY SESSIONS  (anchored to this week's Monday — the calendar is
--    always populated. Rooms: Room 1 = speech, Room 2 = OT, Gym = physio,
--    Room 3 = behaviour/counseling. Past sessions auto-complete below.)
-- ============================================================================
SET @mon = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY);

INSERT INTO therapy_sessions (student_id, therapist_id, therapy_type, start_time, end_time, location, status, notes) VALUES
 -- last week (history)
 (2,3,'SPEECH',       DATE_ADD(@mon, INTERVAL (-3*24+9)  HOUR), DATE_ADD(@mon, INTERVAL (-3*24+10) HOUR),'Room 1','COMPLETED','Good progress on /s/'),
 (1,2,'OCCUPATIONAL', DATE_ADD(@mon, INTERVAL (-3*24+11) HOUR), DATE_ADD(@mon, INTERVAL (-3*24+12) HOUR),'Room 2','COMPLETED','Worked on grip'),
 -- this week: Monday
 (1,3,'SPEECH',       DATE_ADD(@mon, INTERVAL (0*24+9)  HOUR), DATE_ADD(@mon, INTERVAL (0*24+10) HOUR),'Room 1','SCHEDULED',''),
 (3,2,'OCCUPATIONAL', DATE_ADD(@mon, INTERVAL (0*24+9)  HOUR), DATE_ADD(@mon, INTERVAL (0*24+10) HOUR),'Room 2','SCHEDULED',''),
 -- Tuesday
 (2,3,'SPEECH',       DATE_ADD(@mon, INTERVAL (1*24+10) HOUR), DATE_ADD(@mon, INTERVAL (1*24+11) HOUR),'Room 1','SCHEDULED',''),
 (3,2,'PHYSICAL',     DATE_ADD(@mon, INTERVAL (1*24+13) HOUR), DATE_ADD(@mon, INTERVAL (1*24+14) HOUR),'Gym','SCHEDULED',''),
 -- Wednesday
 (1,3,'SPEECH',       DATE_ADD(@mon, INTERVAL (2*24+9)  HOUR), DATE_ADD(@mon, INTERVAL (2*24+10) HOUR),'Room 1','SCHEDULED',''),
 (2,2,'OCCUPATIONAL', DATE_ADD(@mon, INTERVAL (2*24+11) HOUR), DATE_ADD(@mon, INTERVAL (2*24+12) HOUR),'Room 2','SCHEDULED',''),
 -- Thursday
 (3,3,'COUNSELING',   DATE_ADD(@mon, INTERVAL (3*24+10) HOUR), DATE_ADD(@mon, INTERVAL (3*24+11) HOUR),'Room 3','SCHEDULED',''),
 (1,2,'PHYSICAL',     DATE_ADD(@mon, INTERVAL (3*24+14) HOUR), DATE_ADD(@mon, INTERVAL (3*24+15) HOUR),'Gym','SCHEDULED',''),
 -- Friday
 (2,3,'SPEECH',       DATE_ADD(@mon, INTERVAL (4*24+9)  HOUR), DATE_ADD(@mon, INTERVAL (4*24+10) HOUR),'Room 1','SCHEDULED',''),
 (3,2,'BEHAVIORAL',   DATE_ADD(@mon, INTERVAL (4*24+9)  HOUR), DATE_ADD(@mon, INTERVAL (4*24+10) HOUR),'Room 3','SCHEDULED',''),
 -- next week (calendar "next" nav has data too)
 (1,3,'SPEECH',       DATE_ADD(@mon, INTERVAL (7*24+9)  HOUR), DATE_ADD(@mon, INTERVAL (7*24+10) HOUR),'Room 1','SCHEDULED',''),
 (3,2,'OCCUPATIONAL', DATE_ADD(@mon, INTERVAL (7*24+10) HOUR), DATE_ADD(@mon, INTERVAL (7*24+11) HOUR),'Room 2','SCHEDULED','');

-- Sessions earlier this week have already happened → mark completed.
UPDATE therapy_sessions SET status='COMPLETED' WHERE start_time < NOW() AND status='SCHEDULED';

-- ============================================================================
-- 8. BEHAVIOUR INCIDENTS  (spread over the last 4 weeks; one staff-only row)
-- ============================================================================
INSERT INTO behavior_incidents (student_id, reported_by, incident_date, location, description, severity, action_taken, parent_visible) VALUES
 (1,2, DATE_SUB(NOW(), INTERVAL 2  DAY),'Classroom','Refused transition activity','LOW','Redirected with visual schedule',TRUE),
 (1,2, DATE_SUB(NOW(), INTERVAL 10 DAY),'Playground','Pushed a peer','MEDIUM','Calm-down corner, discussed',TRUE),
 (3,2, DATE_SUB(NOW(), INTERVAL 6  DAY),'Cafeteria','Threw food','MEDIUM','Removed from setting',FALSE),
 (2,2, DATE_SUB(NOW(), INTERVAL 18 DAY),'Classroom','Verbal outburst','LOW','Verbal redirection',TRUE),
 (3,2, DATE_SUB(NOW(), INTERVAL 23 DAY),'Hallway','Eloped from line','HIGH','Staff escort, safety review',TRUE);

-- ============================================================================
-- 9. MESSAGES  (teacher ↔ parent, tied to the same stories)
-- ============================================================================
INSERT INTO messages (sender_id, recipient_id, student_id, subject, body, is_read, sent_at) VALUES
 (2,4,1,'Ryan had a great week','Ryan used a 3-word phrase to ask for the iPad — the milestone we have been working toward. Really pleased!', TRUE,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
 (4,2,1,'Re: Ryan had a great week','Thank you so much, that is wonderful to hear. We will keep practising at home.', FALSE, DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (2,5,3,'Jacob''s number work','Jacob counted to 9 today with the blocks. Could you try counting toys with him at home?', FALSE, DATE_SUB(NOW(), INTERVAL 1 DAY)),
 (5,2,2,'Question about Daisy''s speech','Hi James, are there any sounds we should focus on at home for Daisy?', TRUE,  DATE_SUB(NOW(), INTERVAL 4 DAY));

-- ============================================================================
-- 10. ANNOUNCEMENTS  (one per target audience + a general one)
-- ============================================================================
INSERT INTO announcements (title, body, target_role, created_by) VALUES
 ('Welcome to SNE-SPIMS','The progress system is live. Follow your child''s development, reports and schedule here.','ALL',1),
 ('Staff meeting Friday','Weekly case review at 14:00 in the staff room.','TEACHER',1),
 ('Term progress reports published','End-of-term daily reports are now available to view.','PARENT',1),
 ('Family Day next month','Join us for the annual Family Day. Details to follow by email.','ALL',1);

-- ============================================================================
-- 11. NOTIFICATIONS  (drive the bell badge; links point at live pages)
-- ============================================================================
INSERT INTO notifications (user_id, type, title, message, link, student_id, is_read) VALUES
 (4,'DAILY_REPORT','New daily report','A new daily report for Ryan Walker has been published.','/reports/1',1,FALSE),
 (4,'THERAPY_REMINDER','Upcoming therapy session','Speech therapy for Ryan Walker is scheduled this week.','/calendar',1,TRUE),
 (5,'DAILY_REPORT','New daily report','A new daily report for Jacob Cooper has been published.','/reports/6',3,FALSE),
 (5,'THERAPY_REMINDER','Upcoming therapy session','Speech therapy for Daisy Cooper is scheduled this week.','/calendar',2,FALSE),
 (2,'MESSAGE','New message','Robert Walker replied to your message.','/messages',NULL,FALSE),
 (3,'THERAPY_REMINDER','Upcoming therapy session','Counseling session with Jacob Cooper is scheduled this week.','/calendar',3,TRUE);

-- ============================================================================
-- 12. SCHOOL CALENDAR  (shared events on everyone's calendar)
-- ============================================================================
INSERT INTO school_calendar (event_date, event_name, description, created_by) VALUES
 (DATE_ADD(CURDATE(), INTERVAL 5  DAY),'Parent-Teacher Meeting','Quarterly IEP review',1),
 (DATE_ADD(CURDATE(), INTERVAL 12 DAY),'School Holiday','Public holiday',1),
 (DATE_ADD(CURDATE(), INTERVAL 20 DAY),'Family Day','Annual family gathering and activities',1);

-- ============================================================================
-- 13. EXTRA CASELOAD  (gives each teacher a realistic class of 6-10 students)
--     The 3 students above keep the full demo story (IEP / reports).
--     These extra students populate the caseload/roster only.
--       James (id 2): Ryan + Daisy + Jacob + 6 new = 9 students (all Class A)
--       Laura (id 3): 6 new                         = 6 students (all Class B)
-- ============================================================================
INSERT INTO students (id, student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active) VALUES
 ( 4,'SNE-004','Ethan Brooks',  '2016-01-22','MALE',  'LEARNING',       'LOW',     '2023-08-01','Class A','Strong reader; needs support with maths.',TRUE),
 ( 5,'SNE-005','Mia Bennett',   '2015-05-14','FEMALE','HEARING',        'MODERATE','2023-08-01','Class A','Uses a hearing aid; lip-reads well.',TRUE),
 ( 6,'SNE-006','Noah Carter',   '2017-02-09','MALE',  'AUTISM',         'HIGH',    '2024-01-10','Class A','Sensitive to noise; prefers quiet corners.',TRUE),
 ( 7,'SNE-007','Olivia Hayes',  '2016-07-30','FEMALE','PHYSICAL',       'MODERATE','2023-09-05','Class A','Uses a walker; full classroom access.',TRUE),
 ( 8,'SNE-008','Liam Foster',   '2015-12-03','MALE',  'SPEECH_LANGUAGE','LOW',     '2023-08-01','Class A','Expressive delay; benefits from picture cues.',TRUE),
 ( 9,'SNE-009','Ava Reed',      '2016-10-18','FEMALE','MULTIPLE',       'INTENSIVE','2024-02-12','Class A','1:1 support; complex needs.',TRUE),
 (10,'SNE-010','Lucas Gray',    '2014-04-25','MALE',  'INTELLECTUAL',   'MODERATE','2022-08-15','Class B','Friendly; works best with routines.',TRUE),
 (11,'SNE-011','Sophia Price',  '2014-08-11','FEMALE','VISUAL',         'HIGH',    '2023-01-09','Class B','Uses large-print materials.',TRUE),
 (12,'SNE-012','Mason Ward',    '2015-03-07','MALE',  'AUTISM',         'MODERATE','2023-08-01','Class B','Responds well to first-then boards.',TRUE),
 (13,'SNE-013','Isla Hughes',   '2014-11-29','FEMALE','LEARNING',       'LOW',     '2022-09-01','Class B','Confident; needs extended time on tasks.',TRUE),
 (14,'SNE-014','James Bell',    '2015-06-16','MALE',  'SPEECH_LANGUAGE','MODERATE','2023-10-02','Class B','Working on sentence length.',TRUE),
 (15,'SNE-015','Grace Morgan',  '2014-02-21','FEMALE','PHYSICAL',       'HIGH',    '2022-08-15','Class B','Wheelchair user; very independent.',TRUE);

INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES
 (2, 4,TRUE), (2, 5,TRUE), (2, 6,TRUE), (2, 7,TRUE), (2, 8,TRUE), (2, 9,TRUE),   -- James: 6 new (9 total, Class A)
 (3,10,TRUE), (3,11,TRUE), (3,12,TRUE), (3,13,TRUE), (3,14,TRUE), (3,15,TRUE);   -- Laura: 6 new (6 total, Class B)

-- ----------------------------------------------------------------------------
-- 13a. Light IEP per new student (one ACTIVE program + 2 goals each)
--      created_by = the student's primary teacher (James=2 / Laura=3)
-- ----------------------------------------------------------------------------
INSERT INTO iep_programs (id, student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by) VALUES
 ( 4, 4,'IEP Ethan 2024/2025', '2024/2025', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH),'Reads at grade level but struggles with number operations.','Extra time on maths tasks, manipulatives, chunked instructions.','ACTIVE',2),
 ( 5, 5,'IEP Mia 2024/2025',   '2024/2025', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH),'Uses a hearing aid; lip-reads and follows visual cues well.','Preferential seating, face the student when speaking, captions.','ACTIVE',2),
 ( 6, 6,'IEP Noah 2024/2025',  '2024/2025', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 9 MONTH),'Sensitive to noise; seeks quiet spaces when overwhelmed.','Noise-reducing headphones, quiet corner, predictable routine.','ACTIVE',2),
 ( 7, 7,'IEP Olivia 2024/2025','2024/2025', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH),'Uses a walker; participates fully with mobility support.','Accessible seating, extra transition time, clear floor paths.','ACTIVE',2),
 ( 8, 8,'IEP Liam 2024/2025',  '2024/2025', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 9 MONTH),'Expressive language delay; uses picture cues to communicate.','Picture cue cards, model-and-repeat prompts, small groups.','ACTIVE',2),
 ( 9, 9,'IEP Ava 2024/2025',   '2024/2025', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), DATE_ADD(CURDATE(), INTERVAL 7 MONTH),'Complex needs; works with 1:1 support throughout the day.','1:1 aide, sensory breaks, simplified multi-step tasks.','ACTIVE',2),
 (10,10,'IEP Lucas 2024/2025', '2024/2025', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH),'Friendly and routine-driven; benefits from concrete materials.','First-then board, frequent breaks, concrete materials.','ACTIVE',3),
 (11,11,'IEP Sophia 2024/2025','2024/2025', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH),'Low vision; reads with large-print and high-contrast materials.','Large-print materials, high-contrast handouts, front seating.','ACTIVE',3),
 (12,12,'IEP Mason 2024/2025', '2024/2025', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 9 MONTH),'Responds well to first-then boards and visual schedules.','Visual schedule, first-then board, movement breaks.','ACTIVE',3),
 (13,13,'IEP Isla 2024/2025',  '2024/2025', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), DATE_ADD(CURDATE(), INTERVAL 7 MONTH),'Confident learner; needs extended time to process tasks.','Extended time, reduced task load, checklists.','ACTIVE',3),
 (14,14,'IEP James B 2024/2025','2024/2025',DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 9 MONTH),'Speaks in short phrases; working on sentence length.','Sentence starters, model-and-expand prompts, picture cues.','ACTIVE',3),
 (15,15,'IEP Grace 2024/2025', '2024/2025', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH),'Wheelchair user; highly independent in academic work.','Accessible desk height, clear paths, extra transition time.','ACTIVE',3);

INSERT INTO iep_goals (id, iep_id, domain, description, target_date, status, progress_pct) VALUES
 ( 8, 4,'ACADEMIC',        'Add single-digit numbers with manipulatives', DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',40),
 ( 9, 4,'SELF_CARE',       'Organise own materials before each lesson',   DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',55),
 (10, 5,'COMMUNICATION',   'Respond to questions using full sentences',   DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',50),
 (11, 5,'SOCIAL_EMOTIONAL','Join a small-group activity each day',        DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',45),
 (12, 6,'BEHAVIORAL',      'Use a calm-down strategy when overwhelmed',   DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',35),
 (13, 6,'COMMUNICATION',   'Request a break using a card or words',       DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',50),
 (14, 7,'MOTOR',           'Move between activities using the walker',    DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',60),
 (15, 7,'SOCIAL_EMOTIONAL','Participate in group games with peers',       DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',55),
 (16, 8,'COMMUNICATION',   'Use 3-4 word phrases to make requests',       DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',40),
 (17, 8,'SOCIAL_EMOTIONAL','Greet a peer each morning',                   DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',50),
 (18, 9,'SELF_CARE',       'Follow a 2-step self-care routine with cues', DATE_ADD(CURDATE(), INTERVAL 4 MONTH),'IN_PROGRESS',30),
 (19, 9,'COMMUNICATION',   'Indicate choice between two items',           DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',35),
 (20,10,'ACADEMIC',        'Match numerals 1-10 to quantities',           DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',45),
 (21,10,'SELF_CARE',       'Pack own bag at end of day',                  DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',60),
 (22,11,'ACADEMIC',        'Read large-print text for 10 minutes',        DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',55),
 (23,11,'SELF_CARE',       'Locate own belongings independently',         DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',50),
 (24,12,'BEHAVIORAL',      'Follow the first-then board for transitions', DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',50),
 (25,12,'COMMUNICATION',   'Use words to ask for help',                   DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',45),
 (26,13,'ACADEMIC',        'Complete a task within the extended time',    DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',60),
 (27,13,'SOCIAL_EMOTIONAL','Ask a peer for help when stuck',             DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',55),
 (28,14,'COMMUNICATION',   'Produce 5-word sentences with prompting',     DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',45),
 (29,14,'SOCIAL_EMOTIONAL','Take turns in a conversation',               DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',50),
 (30,15,'MOTOR',           'Navigate the classroom independently',        DATE_ADD(CURDATE(), INTERVAL 3 MONTH),'IN_PROGRESS',70),
 (31,15,'ACADEMIC',        'Complete written work at grade level',        DATE_ADD(CURDATE(), INTERVAL 2 MONTH),'IN_PROGRESS',65);

-- ----------------------------------------------------------------------------
-- 13b. One recent published daily report per new student (+ 2 rubrics each)
-- ----------------------------------------------------------------------------
INSERT INTO daily_reports (id, student_id, teacher_id, report_date, summary, mood, status, published_at) VALUES
 ( 8, 4,2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Ethan added single-digit numbers using counters with growing confidence.','HAPPY','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 ( 9, 5,2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Mia answered questions in full sentences during the morning lesson.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (10, 6,2, DATE_SUB(CURDATE(), INTERVAL 3 DAY),'Noah used his quiet corner to self-regulate before group time.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
 (11, 7,2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Olivia moved confidently between activities with her walker.','HAPPY','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (12, 8,2, DATE_SUB(CURDATE(), INTERVAL 3 DAY),'Liam used picture cards to request the materials he needed.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
 (13, 9,2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Ava followed her self-care routine with support from her aide.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (14,10,3, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Lucas matched numerals to quantities up to 8 today.','HAPPY','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (15,11,3, DATE_SUB(CURDATE(), INTERVAL 3 DAY),'Sophia read large-print text for ten minutes without fatigue.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
 (16,12,3, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Mason followed his first-then board through every transition.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (17,13,3, DATE_SUB(CURDATE(), INTERVAL 3 DAY),'Isla completed her task within the extended time independently.','HAPPY','PUBLISHED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
 (18,14,3, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'James produced several five-word sentences during circle time.','CALM','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
 (19,15,3, DATE_SUB(CURDATE(), INTERVAL 2 DAY),'Grace navigated the classroom independently and finished her work.','HAPPY','PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 ( 8,'Academic Engagement','DEVELOPING','Counters helped'),       ( 8,'Emotional Regulation','PROFICIENT',''),
 ( 9,'Communication','PROFICIENT','Full sentences'),              ( 9,'Social Interaction','DEVELOPING',''),
 (10,'Emotional Regulation','DEVELOPING','Used quiet corner'),    (10,'Social Interaction','EMERGING',''),
 (11,'Social Interaction','PROFICIENT','Joined group game'),      (11,'Emotional Regulation','PROFICIENT',''),
 (12,'Communication','DEVELOPING','Picture cards'),               (12,'Academic Engagement','DEVELOPING',''),
 (13,'Emotional Regulation','DEVELOPING','With aide support'),    (13,'Communication','EMERGING',''),
 (14,'Academic Engagement','PROFICIENT','Matched to 8'),          (14,'Emotional Regulation','PROFICIENT',''),
 (15,'Academic Engagement','PROFICIENT','Sustained reading'),     (15,'Emotional Regulation','PROFICIENT',''),
 (16,'Emotional Regulation','PROFICIENT','Smooth transitions'),   (16,'Communication','DEVELOPING',''),
 (17,'Academic Engagement','PROFICIENT','Independent'),           (17,'Social Interaction','DEVELOPING',''),
 (18,'Communication','DEVELOPING','5-word sentences'),            (18,'Social Interaction','DEVELOPING',''),
 (19,'Academic Engagement','PROFICIENT','Independent work'),      (19,'Emotional Regulation','PROFICIENT','');
