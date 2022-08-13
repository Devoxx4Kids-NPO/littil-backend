CREATE TABLE school
(
    school_id           BINARY(16)   NOT NULL,
    school_name         VARCHAR(255) NOT NULL,
    contact_person_name VARCHAR(255) NOT NULL,
    location            BINARY(16)   NOT NULL,
    user                BINARY(16)   NOT NULL,
    created_by          BINARY(16),
    created_date        DATETIME,
    last_modified_by    BINARY(16),
    last_modified_date  DATETIME,
    PRIMARY KEY (school_id),
    CONSTRAINT fk_school_location
        FOREIGN KEY (location)
            REFERENCES location (location_id),
    CONSTRAINT fk_school_user
        FOREIGN KEY (user)
            REFERENCES `user` (user_id)
) ENGINE = INNODB;
