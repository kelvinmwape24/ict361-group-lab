\# Backend Setup Guide



\*\*Author:\*\* Edwin Makuyu (202408031)

\*\*Date:\*\* 2026-09-21



\## Prerequisites

\- Node.js 20 LTS

\- MySQL 8.0+

\- Git for Windows



\## Step 1 — Clone the repository

git clone https://github.com/kelvinmwape24/ict361-group-lab.git

cd ict361-group-lab



\## Step 2 — Create the database

mysql -u root -p < database/schema.sql

mysql -u root -p < database/seed.sql



\## Step 3 — Configure the server

cd server

copy .env.example .env

notepad .env



Change DB\_PASS= to your MySQL password. Save.



\## Step 4 — Install dependencies

npm install



\## Step 5 — Generate the lecturer password hash

node hash.js



Copy the output (starts with $2b$10$).



\## Step 6 — Update the lecturer account

mysql -u root -p



USE ict361\_lab;

UPDATE accounts SET password\_hash='PASTE\_HASH\_HERE' WHERE email='lecturer@mu.ac.zm';

exit;



Replace PASTE\_HASH\_HERE with the hash from Step 5.



\## Step 7 — Start the server

npm run dev



Expected output: Server running on http://localhost:3000



\## Step 8 — Verify

Open a new CMD window:



curl http://localhost:3000



Expected: {"status":"ICT361 server running"}



\## Default Credentials

\- Lecturer: lecturer@mu.ac.zm / admin123



\## Test Data

\- 15 students with claim codes MU-000001 through MU-000015



\## Troubleshooting

| Error | Cause | Fix |

|-------|-------|-----|

| ER\_ACCESS\_DENIED\_ERROR | Wrong DB\_PASS in .env | Edit .env and correct DB\_PASS |

| ECONNREFUSED 3306 | MySQL not running | net start MySQL80 (as admin) |

| EADDRINUSE :::3000 | Old server on port 3000 | netstat -ano \\| findstr :3000, then taskkill /PID <pid> /F |

| INVALID\_CREDENTIALS | Wrong hash in MySQL | Regenerate with node hash.js, update MySQL |

| NO\_TOKEN | Missing Authorization header | Add -H "Authorization: Bearer <token>" |

