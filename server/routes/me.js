const router = require('express').Router();
const pool = require('../db');
const auth = require('../middleware/auth');

router.get('/', auth(['STUDENT']), async (req, res) => {
  const [rows] = await pool.query(
    `SELECT s.*, p.code AS program_code, g.label AS group_label,
            (SELECT COUNT(*) FROM students WHERE group_id = s.group_id AND is_deleted = 0) AS group_count,
            (SELECT capacity FROM lab_groups WHERE group_id = s.group_id) AS group_capacity
     FROM students s
     LEFT JOIN programmes p ON p.program_id = s.program_id
     LEFT JOIN lab_groups g ON g.group_id = s.group_id
     WHERE s.student_id = ? AND s.is_deleted = 0`,
    [req.user.student_id]
  );
  if (!rows.length) return res.status(404).json({ error: 'NOT_FOUND' });
  res.json(rows[0]);
});

router.patch('/', auth(['STUDENT']), async (req, res) => {
  const { name, program_id, version } = req.body;
  if (!name || name.trim().length < 2 || name.trim().length > 100)
    return res.status(400).json({ error: 'INVALID_NAME' });
  const [rows] = await pool.query(
    'SELECT version FROM students WHERE student_id=? AND is_deleted=0',
    [req.user.student_id]
  );
  if (!rows.length) return res.status(404).json({ error: 'NOT_FOUND' });
  if (rows[0].version !== version)
    return res.status(409).json({ error: 'CONFLICT', current_version: rows[0].version });
  await pool.query(
    'UPDATE students SET name=?, program_id=?, version=version+1 WHERE student_id=?',
    [name.trim(), program_id, req.user.student_id]
  );
  res.json({ success: true, new_version: version + 1 });
});

router.post('/group-request', auth(['STUDENT']), async (req, res) => {
  const { group_id } = req.body;
  if (!group_id) return res.status(400).json({ error: 'MISSING_GROUP_ID' });
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    const [groups] = await conn.query(
      'SELECT capacity, label FROM lab_groups WHERE group_id=? FOR UPDATE', [group_id]
    );
    if (!groups.length) {
      await conn.rollback();
      return res.status(404).json({ error: 'GROUP_NOT_FOUND' });
    }
    const [count] = await conn.query(
      'SELECT COUNT(*) AS c FROM students WHERE group_id=? AND is_deleted=0 FOR UPDATE',
      [group_id]
    );
    if (count[0].c >= groups[0].capacity) {
      await conn.rollback();
      return res.status(409).json({ error: 'GROUP_FULL' });
    }
    await conn.query(
      'UPDATE students SET group_id=?, version=version+1 WHERE student_id=? AND is_deleted=0',
      [group_id, req.user.student_id]
    );
    await conn.commit();
    res.json({ success: true, group_label: groups[0].label });
  } catch (e) {
    await conn.rollback();
    res.status(500).json({ error: e.message });
  } finally {
    conn.release();
  }
});

router.post('/number-correction', auth(['STUDENT']), async (req, res) => {
  const { requested_number } = req.body;
  if (!requested_number || !/^\d{9}$/.test(requested_number))
    return res.status(400).json({ error: 'INVALID_STUDENT_NUMBER' });
  res.json({ success: true, status: 'PENDING_LECTURER_APPROVAL', requested_number });
});

module.exports = router;