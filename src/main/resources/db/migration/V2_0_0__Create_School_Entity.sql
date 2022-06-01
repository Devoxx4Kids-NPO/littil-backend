CREATE TABLE school
(
    school_id            BINARY(16)   NOT NULL,
    school_name          VARCHAR(255) NOT NULL,
    address              VARCHAR(255) NOT NULL,
    postal_code          VARCHAR(255) NOT NULL,
    contact_person_name  VARCHAR(255) NOT NULL,
    contact_person_email VARCHAR(255) NOT NULL,
    primary key (school_id)
) ENGINE = INNODB;