CREATE TABLE teacher
(
    teacher_id  BINARY(16) NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    surname     VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    postal_code VARCHAR(255) NOT NULL,
    locale      VARCHAR(255) NOT NULL,
    preferences VARCHAR(255),
    PRIMARY KEY (teacher_id)
) ENGINE=INNODB;

CREATE TABLE teacher_availability
(
    teacher      BINARY(16) NOT NULL,
    availability VARCHAR(255),
    FOREIGN KEY (teacher) REFERENCES teacher (teacher_id)
) ENGINE=INNODB;