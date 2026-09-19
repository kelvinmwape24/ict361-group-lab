const router = require('express').Router();
const { v4: uuidv4 } = require('uuid');
const pool = require('../db');
const auth = require('../middleware/auth');

router.get('/', auth(['LECTURER']), async (req, res) => {
  const { q = '', program = '', group = '', page = 1, size = 10 } = req.query;
  const offset = (page - 1) * size;
  let sql = `SELECT s.*, p.code AS program_code, g.label AS group_label
             FROM students s
             LEFT JOIN programmes p ON p.program_id = s.program_id
             LEFT JOIN lab_groups g ON g.group_id = s.group_id
             WHERE s.is_deleted = 0`;
  const params = [];
  if (q) {
    sql += ' AND (s.name LIKE ? OR s.student_number LIKE ?)';
    params.push(`%${q}%`, `%${q}%`);
  }
  if (program) { sql += ' AND p.code = ?'; params.push(program); }
  if (group === 'UNASSIGNED') { sql += ' AND s.group_id IS NULL'; }
  else if (group) { sql += ' AND g.label = ?'; params.push(group); }
  sql += ' ORDER BY s.name LIMIT ? OFFSET ?';
  params.push(parseInt(size), parseInt(offset));
  const [rows] = await pool.query(sql, params);
  res.json(rows);
});

router.post('/', auth(['LECTURER']), async (req, res) => {
  const { student_number, name, program_id } = req.body;
  if (!/^\d{9}$/.test(student_number))
    return res.status(400).json({ error: 'INVALID_STUDENT_NUMBER' });
  if (!name || name.trim().length < 2)
    return res.status(400).json({ error: 'INVALID_NAME' });
  const [exists] = await pool.query(
    'SELECT 1 FROM students WHERE student_number=?', [student_number]
  );
  if (exists.length) return res.status(409).json({ error: 'DUPLICATE_NUMBER' });
  const id = uuidv4();
  const claim = 'MU-' + Math.floor(100000 + Math.random() * 900000);
  await pool.query(
    'INSERT INTO students (student_id, student_number, name, program_id, claim_code) VALUES (?,?,?,?,?)',
    [id, student_number, name.trim(), program_id, claim]
  );
  res.status(201).json({ student_id: id, claim_code: claim });
});

router.patch('/:id', auth(['LECTURER']), async (req, res) => {
  const { name, program_id, version } = req.body;
  const [rows] = await pool.query(
    'SELECT version FROM students WHERE student_id=? AND is_deleted=0', [req.params.id]
  );
  if (!rows.length) return res.status(404).json({ error: 'NOT_FOUND' });
  if (rows[0].version !== version)
    return res.status(409).json({ error: 'CONFLICT', current_version: rows[0].version });
  await pool.query(
    'UPDATE students SET name=?, program_id=?, version=version+1 WHERE student_id=?',
    [name, program_id, req.params.id]
  );
  res.json({ success: true });
});

router.delete('/:id', auth(['LECTURER']), async (req, res) => {
  await pool.query(
    `UPDATE students SET is_deleted=1, deleted_at=NOW(), group_id=NULL,
     version=version+1 WHERE student_id=? AND is_deleted=0`,
    [req.params.id]
  );
  await pool.query('UPDATE accounts SET is_active=0 WHERE student_id=?', [req.params.id]);
  res.json({ success: true });
});

router.post('/:id/assign', auth(['LECTURER']), async (req, res) => {
  const { group_id } = req.body;
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    const [groups] = await conn.query(
      'SELECT capacity FROM lab_groups WHERE group_id=? FOR UPDATE', [group_id]
    );
    if (!groups.length) throw new Error('GROUP_NOT_FOUND');
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
      [group_id, req.params.id]
    );
    await conn.commit();
    res.json({ success: true });
  } catch (e) {
    await conn.rollback();
    res.status(500).json({ error: e.message });
  } finally {
    conn.release();
  }
});

module.exports = router;