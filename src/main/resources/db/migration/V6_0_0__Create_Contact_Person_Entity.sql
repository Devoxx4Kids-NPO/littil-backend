CREATE TABLE contact_person
(
    contact_person_id  BINARY(16)   NOT NULL,
    first_name         VARCHAR(255)   NOT NULL,
    prefix             VARCHAR(255),
    surname            VARCHAR(255)  NOT NULL,
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (contact_person_id)
) ENGINE = INNODB;