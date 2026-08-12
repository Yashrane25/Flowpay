CREATE TABLE idempotency_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    response_body LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);