CREATE TABLE guest_teacher
(
    guest_teacher_id   BINARY(16)   NOT NULL,
    first_name         VARCHAR(255) NOT NULL,
    surname            VARCHAR(255) NOT NULL,
    location           BINARY(16)   NOT NULL,
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (guest_teacher_id),
    CONSTRAINT fk_teacher_location
        FOREIGN KEY (location)
            REFERENCES location (location_id)
) ENGINE = INNODB;

CREATE TABLE guest_teacher_availability
(
    guest_teacher BINARY(16) NOT NULL,
    availability  VARCHAR(255),
    FOREIGN KEY (guest_teacher) REFERENCES guest_teacher (guest_teacher_id)
) ENGINE = INNODB;
