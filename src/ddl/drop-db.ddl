alter table APP drop constraint APP_TO_ENV_FK;
alter table Env drop constraint ENV_TO_PRODUCT_FK;
drop table if exists APP cascade;
drop table if exists Env cascade;
drop table if exists Product cascade;
drop sequence APP_ID_SEQ;
drop sequence ENV_ID_SEQ;
