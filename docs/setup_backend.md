# Backend Setup Guide

**Author:** Edwin Makuyu (202408031)
**Date:** 2026-09-21

## Prerequisites
- Node.js 20 LTS
- MySQL 8.0+
- Git

## Step 1 — Clone
git clone https://github.com/kelvinmwape24/ict361-group-lab.git
cd ict361-group-lab

## Step 2 — Create database
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql

## Step 3 — Configure
cd server
cp .env.example .env
nano .env
# Set DB_PASS to your MySQL password

## Step 4 — Install
npm install

## Step 5 — Hash lecturer password
node hash.js

## Step 6 — Update MySQL
mysql -u root -p
USE ict361_lab;
UPDATE accounts SET password_hash='PASTE_HASH_HERE' WHERE email='lecturer@mu.ac.zm';
exit;

## Step 7 — Start server
npm run dev
# Expected: Server running on http://localhost:3000

## Step 8 — Verify
curl http://localhost:3000
# Expected: {"status":"ICT361 server running"}

## Troubleshooting
| Error | Fix |
|-------|-----|
| ER_ACCESS_DENIED | Fix DB_PASS in .env |
| ECONNREFUSED 3306 | net start MySQL80 |
| EADDRINUSE 3000 | kill the process on port 3000 |

## Default credentials
- Lecturer: lecturer@mu.ac.zm / admin123
