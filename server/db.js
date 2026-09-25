const mysql = require('mysql2/promise');
require('dotenv').config();

const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

cat > server/hash.js << 'EOF'
const bcrypt = require('bcrypt');
bcrypt.hash('admin123', 10).then(h => console.log(h));
