# ICT361 API Contract

## Base URL
http://localhost:3000 (dev)
http://10.0.2.2:3000 (Android emulator)

## Endpoints

POST /auth/login → { email, password } → 200: { token, role }
POST /auth/register → { claim_code, name, student_number, password } → 201
GET /students → LECTURER → query: q, program, group, page, size
POST /students → LECTURER → { student_number, name, program_id }
PATCH /students/:id → LECTURER → { name, program_id, version }
DELETE /students/:id → LECTURER → soft delete
POST /students/:id/assign → LECTURER → { group_id }
GET /students/me → STUDENT
PATCH /students/me → STUDENT
POST /sync → { operation_id, type, payload, base_version }

## Git Bash curl Examples

### Login
curl -X POST http://localhost:3000/auth/login -H "Content-Type: application/json" -d '{"email":"lecturer@mu.ac.zm","password":"admin123"}'

### List students
TOKEN="paste-token"
curl "http://localhost:3000/students?size=100" -H "Authorization: Bearer $TOKEN"

### Register
curl -X POST http://localhost:3000/auth/register -H "Content-Type: application/json" -d '{"claim_code":"MU-000015","name":"Edwin Makuyu","student_number":"202408031","password":"test1234"}'
