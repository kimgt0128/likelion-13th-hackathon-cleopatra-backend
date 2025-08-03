-- 데이터베이스 생성 (이미 존재하면 제외)
CREATE DATABASE IF NOT EXISTS cleopatra CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cleopatra;

-- member 테이블 생성: username 컬럼 단일 칼럼으로 구성
CREATE TABLE IF NOT EXISTS member (
                                      username VARCHAR(50) NOT NULL PRIMARY KEY
);

-- 인덱스나 제약조건 단순 예시 (필요에 따라 확장 가능)
