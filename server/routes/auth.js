const router = require('express').Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const pool = require('../db');

router.post('/login', async (req, res) => {
  const { email, password } = req.body;
  const [rows] = await pool.query(
    'SELECT * FROM accounts WHERE email=? AND is_active=1', [email]
  );
  if (!rows.length) return res.status(401).json({ error: 'INVALID_CREDENTIALS' });
  const user = rows[0];
  const ok = await bcrypt.compare(password, user.password_hash);
  if (!ok) return res.status(401).json({ error: 'INVALID_CREDENTIALS' });
  const token = jwt.sign(
    { account_id: user.account_id, role: user.role, student_id: user.student_id },
    process.env.JWT_SECRET,
    { expiresIn: '8h' }
  );
  res.json({ token, role: user.role, student_id: user.student_id });
});

router.post('/register', async (req, res) => {
  const { claim_code, name, student_number, password } = req.body;
  if (!claim_code || !name || !student_number || !password)
    return res.status(400).json({ error: 'MISSING_FIELDS' });
  if (!/^\d{9}$/.test(student_number))
    return res.status(400).json({ error: 'INVALID_STUDENT_NUMBER' });
  if (name.trim().length < 2 || name.trim().length > 100)
    return res.status(400).json({ error: 'INVALID_NAME' });
  if (password.length < 6)
    return res.status(400).json({ error: 'WEAK_PASSWORD' });

  const [students] = await pool.query(
    'SELECT * FROM students WHERE claim_code=? AND student_number=? AND is_deleted=0',
    [claim_code, student_number]
  );
  if (!students.length) return res.status(404).json({ error: 'CLAIM_CODE_NOT_FOUND' });
  const student = students[0];

  const [existing] = await pool.query(
    'SELECT 1 FROM accounts WHERE student_id=?', [student.student_id]
  );
  if (existing.length) return res.status(409).json({ error: 'ALREADY_REGISTERED' });

  const accountId = uuidv4();
  const hash = await bcrypt.hash(password, 10);

  await pool.query(
    `INSERT INTO accounts (account_id, student_id, email, password_hash, role, is_active)
     VALUES (?, ?, ?, ?, 'STUDENT', 1)`,
    [accountId, student.student_id, student_number + '@student.mu', hash]
  );

  const token = jwt.sign(
    { account_id: accountId, role: 'STUDENT', student_id: student.student_id },
    process.env.JWT_SECRET,
    { expiresIn: '8h' }
  );

  res.status(201).json({ token, role: 'STUDENT', student_id: student.student_id });
});

module.exports = router;