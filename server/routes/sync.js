const router = require('express').Router();
const crypto = require('crypto');
const pool = require('../db');
const auth = require('../middleware/auth');

function hashPayload(p) {
  return crypto.createHash('sha256').update(JSON.stringify(p)).digest('hex');
}

router.post('/', auth(), async (req, res) => {
  const { operation_id, type, payload, base_version } = req.body;
  const accountId = req.user.account_id;

  const [existing] = await pool.query(
    'SELECT payload_hash, result_json FROM operation_receipts WHERE operation_id=?',
    [operation_id]
  );
  if (existing.length) {
    if (existing[0].payload_hash !== hashPayload(payload))
      return res.status(409).json({ error: 'OPERATION_ID_REUSED' });
    return res.json(JSON.parse(existing[0].result_json));
  }

  let result;
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    if (type === 'UPDATE') {
      const [rows] = await conn.query(
        'SELECT version FROM students WHERE student_id=? AND is_deleted=0 FOR UPDATE',
        [payload.student_id]
      );
      if (!rows.length) {
        await conn.rollback();
        return res.status(410).json({ error: 'STUDENT_DELETED' });
      }
      if (rows[0].version !== base_version) {
        await conn.rollback();
        return res.status(409).json({ error: 'CONFLICT', current_version: rows[0].version });
      }
      await conn.query(
        'UPDATE students SET name=?, program_id=?, version=version+1 WHERE student_id=?',
        [payload.name, payload.program_id, payload.student_id]
      );
      result = { success: true, new_version: base_version + 1 };
    } else if (type === 'ASSIGN') {
      const [groups] = await conn.query(
        'SELECT capacity FROM lab_groups WHERE group_id=? FOR UPDATE', [payload.group_id]
      );
      const [count] = await conn.query(
        'SELECT COUNT(*) AS c FROM students WHERE group_id=? AND is_deleted=0 FOR UPDATE',
        [payload.group_id]
      );
      if (count[0].c >= groups[0].capacity) {
        await conn.rollback();
        return res.status(409).json({ error: 'GROUP_FULL' });
      }
      await conn.query(
        'UPDATE students SET group_id=?, version=version+1 WHERE student_id=?',
        [payload.group_id, payload.student_id]
      );
      result = { success: true };
    } else {
      result = { success: true, skipped: true };
    }

    await conn.query(
      'INSERT INTO operation_receipts (operation_id, account_id, payload_hash, result_json) VALUES (?,?,?,?)',
      [operation_id, accountId, hashPayload(payload), JSON.stringify(result)]
    );
    await conn.commit();
    res.json(result);
  } catch (e) {
    await conn.rollback();
    res.status(500).json({ error: e.message });
  } finally {
    conn.release();
  }
});

module.exports = router;