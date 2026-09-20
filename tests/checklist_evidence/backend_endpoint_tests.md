\# Backend Endpoint Tests — Kansamba Auxiria



\## Test 1 — Login (valid credentials)

Command: POST /auth/login

Expected: 200 + token + role LECTURER

Actual: PASTE\_HERE



\## Test 2 — Login (wrong password)

Expected: 401 INVALID\_CREDENTIALS

Actual: PASTE\_HERE



\## Test 3 — List students (valid token)

Expected: 200 + 15 students

Actual: PASTE\_HERE



\## Test 4 — List students (no token)

Expected: 401 NO\_TOKEN

Actual: PASTE\_HERE



\## Test 5 — Search "kelvin"

Expected: 200 + 1 result

Actual: PASTE\_HERE



\## Test 6 — Filter group G01

Expected: 200 + 0 results (no one assigned yet)

Actual: PASTE\_HERE



\## Test 7 — Filter UNASSIGNED

Expected: 200 + 15 results

Actual: PASTE\_HERE



\## Test 8 — Filter programme CS

Expected: 200 + 15 results

Actual: PASTE\_HERE



\## Test 9 — Register Edwin with claim code MU-000015

Expected: 201 + token + role STUDENT

Actual: PASTE\_HERE



\## Test 10 — Register with wrong claim code

Expected: 404 CLAIM\_CODE\_NOT\_FOUND

Actual: PASTE\_HERE



\## Test 11 — Duplicate student number

Expected: 409 DUPLICATE\_NUMBER

Actual: PASTE\_HERE



\## Test 12 — Invalid student number (8 digits)

Expected: 400 INVALID\_STUDENT\_NUMBER

Actual: PASTE\_HERE



\## Test 13 — Invalid name (1 char)

Expected: 400 INVALID\_NAME

Actual: PASTE\_HERE



\## Test 14 — SQL injection attempt

Expected: 200 + 0 rows

Actual: PASTE\_HERE

