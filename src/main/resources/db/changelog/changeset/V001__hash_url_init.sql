CREATE SEQUENCE unique_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE hash (
    hash VARCHAR(6) PRIMARY KEY
);

CREATE TABLE url (
    hash       VARCHAR(6) PRIMARY KEY,
    url        VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_url_hash FOREIGN KEY (hash) REFERENCES hash (hash)
);