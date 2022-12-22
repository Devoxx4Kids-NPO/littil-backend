create table user_setting
(
    user_id          BINARY(16)   not null,
    setting_key      VARCHAR(255) not null,
    setting_value    VARCHAR(255) not null,
    insert_timestamp timestamp,
    update_timestamp timestamp,
    PRIMARY KEY (user_id, setting_key)
) ENGINE = INNODB;