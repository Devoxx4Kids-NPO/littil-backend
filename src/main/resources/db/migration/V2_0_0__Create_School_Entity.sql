CREATE TABLE school
(
    school_id           BINARY(16)   NOT NULL,
    school_name         VARCHAR(255) NOT NULL,
    contact_person_name VARCHAR(255) NOT NULL,
    location            BINARY(16)   NOT NULL,
    created_by          VARCHAR(255),
    created_date        DATETIME,
    last_modified_by    VARCHAR(255),
    last_modified_date  DATETIME,
    PRIMARY KEY (school_id),
    CONSTRAINT fk_school_location
        FOREIGN KEY (location)
            REFERENCES location (location_id)
) ENGINE = INNODB;
