CREATE TABLE school_module
(
    id          BINARY(16)   NOT NULL,
    school_id   BINARY(16)   NOT NULL,
    module_id   BINARY(16)   NOT NULL,
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_schoolmodule_school
        FOREIGN KEY (school_id)
            REFERENCES school (school_id),
    CONSTRAINT fk_schoolmodule_module
        FOREIGN KEY (module_id)
            REFERENCES module (module_id)
) ENGINE = INNODB;
