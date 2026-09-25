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


cat > server/routes/me.js << 'EOF'
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

cat > server/routes/sync.js << 'EOF'
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

cat > server/server.js << 'EOF'
const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '1mb' }));

app.use('/auth', require('./routes/auth'));
app.use('/students/me', require('./routes/me'));
app.use('/students', require('./routes/students'));
app.use('/sync', require('./routes/sync'));

app.get('/', (req, res) => res.json({ status: 'ICT361 server running' }));

app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: 'SERVER_ERROR' });
});

app.listen(process.env.PORT, () => {
  console.log(`Server running on http://localhost:${process.env.PORT}`);
});
