USE ict361_lab;

-- Lecturer account (password: admin123 — run `node server/hash.js` to generate the real hash)
INSERT INTO accounts (account_id, email, password_hash, role)
VALUES (UUID(), 'lecturer@mu.ac.zm', 'PLACEHOLDER', 'LECTURER');

-- 15 team members
INSERT INTO students (student_id, student_number, name, program_id, group_id, claim_code)
VALUES
(UUID(), '202203897', 'Kelvin Mwape', 1, NULL, 'MU-000001'),
(UUID(), '202305732', 'Salima Banda', 1, NULL, 'MU-000002'),
(UUID(), '202405943', 'Mapalo Chilufya', 1, NULL, 'MU-000003'),
(UUID(), '202403846', 'Mordecai Salim Traore', 1, NULL, 'MU-000004'),
(UUID(), '202403844', 'Lamin Traore', 1, NULL, 'MU-000005'),
(UUID(), '202404688', 'Mbasela Fabian', 1, NULL, 'MU-000006'),
(UUID(), '202001699', 'Chibesa Mumbi', 1, NULL, 'MU-000007'),
(UUID(), '202401150', 'Mainza Muunda', 1, NULL, 'MU-000008'),
(UUID(), '202403019', 'Agrippa C. Hamasukwa', 1, NULL, 'MU-000009'),
(UUID(), '202303375', 'Racheal Daka', 1, NULL, 'MU-000010'),
(UUID(), '202206607', 'Kansamba Auxiria', 1, NULL, 'MU-000011'),
(UUID(), '202204674', 'Collins Chanda', 1, NULL, 'MU-000012'),
(UUID(), '202403552', 'Katanga Miti', 1, NULL, 'MU-000013'),
(UUID(), '202404172', 'Ben Chola', 1, NULL, 'MU-000014'),
(UUID(), '202408031', 'Edwin Makuyu', 1, NULL, 'MU-000015');