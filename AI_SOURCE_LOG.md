# AI / Source Log — ICT361 Group Lab

| # | Tool / Source | Question Asked | Code Affected | Change Made | Verified |
|---|---------------|----------------|---------------|-------------|----------|
| 1 | DeepSeek (chat) | How to structure a Node.js + MySQL backend | server/server.js, server/routes/*, server/db.js | Generated initial Express app skeleton | Ran npm run dev, tested all endpoints |
| 2 | DeepSeek (chat) | How to prevent two clients from taking the last group slot | server/routes/students.js | Added MySQL transaction with SELECT ... FOR UPDATE | Challenge 1 test passed |
| 3 | DeepSeek (chat) | How to make sync idempotent | server/routes/sync.js | Added operation_receipts table + payload hash check | Retried same operation_id twice, got 1 record |
| 4 | Official MySQL docs | Transaction isolation levels | server/routes/students.js | Confirmed FOR UPDATE behaviour in InnoDB | Read docs at dev.mysql.com |
| 5 | Express docs | Route structure and middleware ordering | server/server.js | Wired /auth, /students/me, /students, /sync | Tested each endpoint |

**Rules followed:**
- No real student data uploaded to AI tools
- No passwords or JWT secrets shared with AI tools
- All AI-generated code was reviewed, tested, and adapted
