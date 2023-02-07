
CREATE TABLE module
(
    module_id          BINARY(16)   NOT NULL,
    module_name        VARCHAR(50)  NOT NULL,
    deleted            tinyint(1) default 0,
    created_by         BINARY(16),
    created_date       DATETIME,
    last_modified_by   BINARY(16),
    last_modified_date DATETIME,
    PRIMARY KEY (module_id)
) ENGINE = INNODB;

alter table module
    add constraint UK_module_module_name unique (module_name);

insert into module (module_id, module_name, deleted)
values (0, "Scratch",0),
       (1, "CodeCombat",0),
       (2, "MBot's",0),
       (3, "Lego Mindstorms",0),
       (4, "Lego WeDo",0),
       (5, "Hedycode",0);

update module set created_date = current_time;
