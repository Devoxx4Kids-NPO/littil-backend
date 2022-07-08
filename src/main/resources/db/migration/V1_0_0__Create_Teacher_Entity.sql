CREATE TABLE teacher
(
    teacher_id         BINARY(16)   NOT NULL,
    first_name         VARCHAR(255) NOT NULL,
    surname            VARCHAR(255) NOT NULL,
    location           BINARY(16)   NOT NULL,
    created_by         VARCHAR(255),
    created_date       DATETIME,
    last_modified_by   VARCHAR(255),
    last_modified_date DATETIME,
    PRIMARY KEY (teacher_id),
    CONSTRAINT fk_teacher_location
        FOREIGN KEY (location)
            REFERENCES location (location_id)
) ENGINE=INNODB;

CREATE TABLE teacher_availability
(
    teacher      BINARY(16) NOT NULL,
    availability VARCHAR(255),
    FOREIGN KEY (teacher) REFERENCES teacher (teacher_id)
) ENGINE=INNODB;
