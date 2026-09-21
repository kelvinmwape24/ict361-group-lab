\# Backend Endpoint Tests — Kansamba Auxiria (202206607)



\*\*Date:\*\* 2026-09-21

\*\*Server:\*\* http://localhost:3000

\*\*Lecturer:\*\* lecturer@mu.ac.zm / admin123



\---



\## Test 1 — Login (valid)

Command: `curl -X POST http://localhost:3000/auth/login -H "Content-Type: application/json" -d "{\\"email\\":\\"lecturer@mu.ac.zm\\",\\"password\\":\\"admin123\\"}"`



Expected: 200 + token + role LECTURER



Actual:

{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...","role":"LECTURER","student\_id":null}



Result: PASS



\---



\## Test 2 — Login (wrong password)

Command: `curl -X POST http://localhost:3000/auth/login -H "Content-Type: application/json" -d "{\\"email\\":\\"lecturer@mu.ac.zm\\",\\"password\\":\\"wrong\\"}"`



Expected: 401 INVALID\_CREDENTIALS



Actual:

{"error":"INVALID\_CREDENTIALS"}



Result: PASS



\---



\## Test 3 — List students (valid token)

Command: `curl "http://localhost:3000/students?size=100" -H "Authorization: Bearer <TOKEN>"`



Expected: 200 + 15 students



Actual: 15 students returned (Agrippa, Ben, Chibesa, Collins, Edwin, Kansamba, Katanga, Kelvin, Lamin, Mainza, Mapalo, Mbasela, Mordecai, Racheal, Salima)



Result: PASS



\---



\## Test 4 — No token

Command: `curl "http://localhost:3000/students?size=100"`



Expected: 401 NO\_TOKEN



Actual:

{"error":"NO\_TOKEN"}



Result: PASS



\---



\## Test 5 — Search Kelvin

Command: `curl "http://localhost:3000/students?q=kelvin" -H "Authorization: Bearer <TOKEN>"`



Expected: 200 + 1 result



Actual: 1 result (Kelvin Mwape, 202203897)



Result: PASS



\---



\## Test 6 — Filter G01

Command: `curl "http://localhost:3000/students?group=G01\&size=100" -H "Authorization: Bearer <TOKEN>"`



Expected: 200 + G01 students



Actual: 14 students in G01 (all except Edwin)



Result: PASS



\---



\## Test 7 — Filter Unassigned

Command: `curl "http://localhost:3000/students?group=UNASSIGNED" -H "Authorization: Bearer <TOKEN>"`



Expected: 200 + unassigned students



Actual: 1 student (Edwin Makuyu, 202408031)



Result: PASS



\---



\## Test 8 — Filter CS

Command: `curl "http://localhost:3000/students?program=CS" -H "Authorization: Bearer <TOKEN>"`



Expected: 200 + CS students



Actual: All 15 students (all in CS programme)



Result: PASS



\---



\## Test 9 — Register student (valid claim code)

Command: `curl -X POST http://localhost:3000/auth/register -H "Content-Type: application/json" -d "{\\"claim\_code\\":\\"MU-000002\\",\\"name\\":\\"Salima Banda\\",\\"student\_number\\":\\"202305732\\",\\"password\\":\\"test1234\\"}"`



Expected: 201 + token + role STUDENT, OR 409 ALREADY\_REGISTERED



Actual:

{"error":"ALREADY\_REGISTERED"}



Result: PASS (correctly rejects duplicate registration)



\---



\## Test 10 — Register with wrong claim code

Command: Same as Test 9 with claim\_code=WRONG



Expected: 404 CLAIM\_CODE\_NOT\_FOUND



Actual:

{"error":"CLAIM\_CODE\_NOT\_FOUND"}



Result: PASS



\---



\## Test 11 — Duplicate student number

Command: `curl -X POST http://localhost:3000/students -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" -d "{\\"student\_number\\":\\"202203897\\",\\"name\\":\\"Test Dupe\\",\\"program\_id\\":1}"`



Expected: 409 DUPLICATE\_NUMBER



Actual:

{"error":"DUPLICATE\_NUMBER"}



Result: PASS



\---



\## Test 12 — Invalid 8-digit number

Command: Same as Test 11 with student\_number=20220389



Expected: 400 INVALID\_STUDENT\_NUMBER



Actual:

{"error":"INVALID\_STUDENT\_NUMBER"}



Result: PASS



\---



\## Test 13 — Invalid 1-character name

Command: Same as Test 11 with name=A



Expected: 400 INVALID\_NAME



Actual:

{"error":"INVALID\_NAME"}



Result: PASS



\---



\## Test 14 — SQL injection attempt

Command: `curl -G "http://localhost:3000/students" --data-urlencode "q=' OR 1=1--" -H "Authorization: Bearer <TOKEN>"`



Expected: 200 + 0 rows (parameterised query blocks injection)



Actual: \[]



Result: PASS



\---



\## Summary



\- Total tests: 14

\- Passed: 14

\- Failed: 0



\*\*Notes:\*\*

\- All endpoints return correct error codes for invalid input.

\- Duplicate student numbers are rejected.

\- Invalid field lengths are rejected.

\- JWT authentication is required on protected routes.

\- Parameterised SQL blocks injection attempts (returns 0 rows instead of leaking data).

\- Group capacity is enforced at DB level.



\*\*Tester:\*\* Kansamba Auxiria (202206607)

