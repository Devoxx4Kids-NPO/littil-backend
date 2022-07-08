CREATE TABLE location
(
    location_id        BINARY(16)   NOT NULL,
    address            VARCHAR(255) NOT NULL,
    postal_code        VARCHAR(255) NOT NULL,
    latitude           MEDIUMINT    NOT NULL,
    longitude          MEDIUMINT    NOT NULL,
    created_by         VARCHAR(255),
    created_date       DATETIME,
    last_modified_by   VARCHAR(255),
    last_modified_date DATETIME,
    PRIMARY KEY (location_id)
) ENGINE = INNODB;


