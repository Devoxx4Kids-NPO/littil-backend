CREATE TABLE user
(
    user_id            BINARY(16)   NOT NULL,
    provider           VARCHAR(50)  NOT NULL,
    provider_id        VARCHAR(255) NOT NULL,
    email_address      VARCHAR(255) NOT NULL UNIQUE,
    guest_teacher      BINARY(16),
    school             BINARY(16),
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (user_id),
    CONSTRAINT uc_user_email_address UNIQUE (email_address),
    CONSTRAINT fk_user_guest_teacher
        FOREIGN KEY (guest_teacher)
            REFERENCES guest_teacher (guest_teacher_id),
    CONSTRAINT fk_user_school
        FOREIGN KEY (school)
            REFERENCES school (school_id)
) ENGINE=INNODB;
