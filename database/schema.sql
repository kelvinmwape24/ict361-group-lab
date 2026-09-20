CREATE DATABASE IF NOT EXISTS ict361_lab CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ict361_lab;

CREATE TABLE programmes (
  program_id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(10) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE lab_groups (
  group_id INT PRIMARY KEY AUTO_INCREMENT,
  label VARCHAR(5) UNIQUE NOT NULL,
  capacity INT NOT NULL DEFAULT 15
) ENGINE=InnoDB;

CREATE TABLE students (
  student_id CHAR(36) PRIMARY KEY,
  student_number CHAR(9) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  program_id INT,
  group_id INT NULL,
  version INT DEFAULT 1,
  is_deleted TINYINT DEFAULT 0,
  deleted_at DATETIME NULL,
  claim_code VARCHAR(20) UNIQUE NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (program_id) REFERENCES programmes(program_id),
  FOREIGN KEY (group_id) REFERENCES lab_groups(group_id)
) ENGINE=InnoDB;

CREATE TABLE accounts (
  account_id CHAR(36) PRIMARY KEY,
  student_id CHAR(36) NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('STUDENT','LECTURER') NOT NULL,
  is_active TINYINT DEFAULT 1,
  FOREIGN KEY (student_id) REFERENCES students(student_id)
) ENGINE=InnoDB;

CREATE TABLE operation_receipts (
  operation_id CHAR(36) PRIMARY KEY,
  account_id CHAR(36) NOT NULL,
  payload_hash CHAR(64) NOT NULL,
  result_json TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO programmes (code) VALUES ('CS'), ('IT'), ('DS');
INSERT INTO lab_groups (label) VALUES ('G01'), ('G02'), ('G03'), ('G04');