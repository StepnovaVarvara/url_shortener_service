CREATE TABLE hash (
    id BIGSERIAL PRIMARY KEY,
    hash VARCHAR(7) NOT NULL
);

CREATE SEQUENCE unique_hash_number_sequences
    START WITH 56800235584
    INCREMENT BY 1;