alter table APP drop constraint APP_TO_ENV_FK;
drop table if exists APP cascade;
drop table if exists Env cascade;
drop sequence APP_ID_SEQ;
