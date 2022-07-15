create table user_setting
(
    user_id          BINARY(16)   not null,
    `key`            VARCHAR(255) not null,
    value            VARCHAR(255) not null,
    insert_timestamp timestamp,
    update_timestamp timestamp,
    constraint pk_customer_setting primary key (user_id, `key`)
) ENGINE = INNODB;