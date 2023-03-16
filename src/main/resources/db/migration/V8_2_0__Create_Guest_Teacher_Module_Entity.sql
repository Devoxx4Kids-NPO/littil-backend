CREATE TABLE guest_teacher_module
(
    id                 BINARY(16)   NOT NULL,
    guest_teacher_id   BINARY(16)   NOT NULL,
    module_id          BINARY(16)   NOT NULL,
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_guest_teacher_module
        FOREIGN KEY (module_id)
            REFERENCES module (module_id)
) ENGINE = INNODB;
