CREATE TABLE location
(
    location_id        BINARY(16)   NOT NULL,
    country_code       VARCHAR(2)   NOT NULL,
    address            VARCHAR(255) NOT NULL,
    postal_code        VARCHAR(10)  NOT NULL,
    latitude           MEDIUMINT    NOT NULL,
    longitude          MEDIUMINT    NOT NULL,
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (location_id)
) ENGINE = INNODB;


