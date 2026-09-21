USE ict361_lab;

-- Reset all group assignments
UPDATE students SET group_id = NULL;

-- Put 13 students in G01 for Challenge 1 testing
UPDATE students SET group_id = 1
WHERE student_number NOT IN ('202408031', '202401150')
  AND is_deleted = 0;

-- Show final counts
SELECT group_id, COUNT(*) AS members FROM students WHERE is_deleted = 0 GROUP BY group_id;