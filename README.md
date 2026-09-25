# ICT361 Group Lab — Android Student Registration & Lab Group Management

Mulungushi University · School of Engineering and Technology
ICT361 Mobile Application Development

## Structure
- `server/` — Node.js / Express backend
- `database/` — MySQL scripts
- `docs/` — Architecture, API contract, ER diagram
- `tests/` — Challenge evidence
- `android/` — Android app

## Quick Start (Backend)
git clone https://github.com/kelvinmwape24/ict361-group-lab.git
cd ict361-group-lab
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
cd server
cp .env.example .env
nano .env
npm install
npm run dev

## Default Credentials
Lecturer: lecturer@mu.ac.zm / admin123
