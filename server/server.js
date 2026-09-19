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