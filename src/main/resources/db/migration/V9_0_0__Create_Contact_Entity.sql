CREATE TABLE contact
(
    id                 BINARY(16)   NOT NULL,
    recipient_id       BINARY(16)   NOT NULL,
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_contact_recipient
        FOREIGN KEY (recipient_id)
            REFERENCES `user` (user_id)
) ENGINE = INNODB;