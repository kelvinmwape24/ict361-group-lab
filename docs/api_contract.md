# ICT361 API Contract

## Base URL
- Dev: http://localhost:3000
- Emulator: http://10.0.2.2:3000

## Endpoints

### POST /auth/login
Public. Body: { email, password }
200: { token, role, student_id }
401: { error: "INVALID_CREDENTIALS" }

### POST /auth/register
Public. Body: { claim_code, name, student_number, password }
201: { token, role: "STUDENT", student_id }
400: INVALID_STUDENT_NUMBER / INVALID_NAME / WEAK_PASSWORD
404: CLAIM_CODE_NOT_FOUND
409: ALREADY_REGISTERED

### GET /students
LECTURER only. Query: q, program, group, page, size
200: [ StudentDto ]

### POST /students
LECTURER only. Body: { student_number, name, program_id }
201: { student_id, claim_code }
409: DUPLICATE_NUMBER

### PATCH /students/:id
LECTURER only. Body: { name, program_id, version }
200: { success: true }
409: CONFLICT

### DELETE /students/:id
LECTURER only. Soft delete.
200: { success: true }

### POST /students/:id/assign
LECTURER only. Body: { group_id }
200: { success: true }
409: GROUP_FULL

### GET /students/me
STUDENT only. 200: own profile

### PATCH /students/me
STUDENT only. Body: { name, program_id, version }

### POST /students/me/group-request
STUDENT only. Body: { group_id }

### POST /sync
Authenticated. Body: { operation_id, type, payload, base_version }
200: { success: true, new_version }
409: CONFLICT / GROUP_FULL / OPERATION_ID_REUSED
410: STUDENT_DELETED

## Git Bash curl Examples

### Login
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"lecturer@mu.ac.zm","password":"admin123"}'

### List students
TOKEN="paste-token"
curl "http://localhost:3000/students?size=100" \
  -H "Authorization: Bearer $TOKEN"

### Register
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{"claim_code":"MU-000015","name":"Edwin Makuyu","student_number":"202408031","password":"test1234"}'

### Assign group
curl -X POST http://localhost:3000/students/UUID/assign \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"group_id":1}'

### Sync update
curl -X POST http://localhost:3000/sync \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"operation_id":"op-001","type":"UPDATE","payload":{"student_id":"UUID","name":"New","program_id":1},"base_version":3}'
