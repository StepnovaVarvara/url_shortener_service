CREATE TABLE urls(
    hash VARCHAR(6) PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hashes(
    hash VARCHAR(255) PRIMARY KEY,
    CONSTRAINT unique_number_seq_seq
            UNIQUE (nextval('unique_number_seq'))
);

CREATE SEQUENCE unique_number_seq
    START WITH 1
    INCREMENT BY 1;