\# ICT361 API Contract



\## Base URL

\- Dev: http://localhost:3000

\- Emulator: http://10.0.2.2:3000

\- Production: HTTPS only



\## Authentication

All protected routes require: `Authorization: Bearer <JWT>`

Server checks identity, role, and record ownership on every request.



\## Endpoints



\### POST /auth/login

Role: public

Request: { "email": "string", "password": "string" }

Responses:

\- 200 { "token": "jwt", "role": "STUDENT|LECTURER" }

\- 401 { "error": "INVALID\_CREDENTIALS" }



\### GET /students

Role: LECTURER

Query: q, program, group, page, size

Responses:

\- 200 \[ { student\_id, student\_number, name, program\_code, group\_label, version } ]

\- 401 NO\_TOKEN / INVALID\_TOKEN

\- 403 FORBIDDEN



\### POST /students

Role: LECTURER

Request: { student\_number, name, program\_id }

Responses:

\- 201 { student\_id }

\- 400 INVALID\_STUDENT\_NUMBER / INVALID\_NAME

\- 409 DUPLICATE\_NUMBER



\### PATCH /students/:id

Role: LECTURER

Request: { name, program\_id, version }

Responses:

\- 200 { success: true }

\- 404 NOT\_FOUND

\- 409 { error: "CONFLICT", current\_version }



\### DELETE /students/:id

Role: LECTURER

Responses:

\- 200 { success: true }  (soft delete: is\_deleted=1, group\_id=NULL, account disabled)



\### POST /students/:id/assign

Role: LECTURER

Request: { group\_id }

Responses:

\- 200 { success: true }

\- 409 GROUP\_FULL

Rules:

\- MySQL transaction with SELECT ... FOR UPDATE on lab\_groups row

\- Capacity enforced at DB level (never Android-only)



\### POST /sync

Role: any authenticated

Request: {

&#x20; operation\_id: "uuid",

&#x20; type: "UPDATE|ASSIGN",

&#x20; payload: {...},

&#x20; base\_version: int

}

Responses:

\- 200 { success: true, new\_version }

\- 409 CONFLICT

\- 409 GROUP\_FULL

\- 409 OPERATION\_ID\_REUSED

\- 410 STUDENT\_DELETED

Rules:

\- operation\_receipts table with operation\_id primary key

\- Same operation\_id + same payload hash → replay stored result

\- Same operation\_id + different payload → 409

\- base\_version compared before accepting edit

