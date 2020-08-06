create sequence APP_ID_SEQ start 1 increment 1;
create sequence ENV_ID_SEQ start 1 increment 1;
create table APP (APP_ID int8 not null, DEPLOYED boolean not null, DEPLOYED_DATE varchar(255), APP_NAME varchar(255) not null, UPDATED_DATE varchar(255) not null, VERSION varchar(255) not null, ENV_ID int8 not null, primary key (APP_ID));
create table Env (ENV_ID int8 not null, DISPLAY_NAME varchar(255) not null, LIVE boolean, ENV_NAME varchar(255) not null, NEXT_ENV_ID varchar(255), TESTED boolean, UPDATED_DATE varchar(255) not null, PRODUCT_ID varchar(255) not null, primary key (ENV_ID));
create table Product (PRODUCT_ID varchar(255) not null, DESCRIPTION varchar(255) not null, REPO varchar(255) not null, VERSION varchar(255) not null, primary key (PRODUCT_ID));
alter table APP add constraint ENV_APP_KEY unique (ENV_ID, APP_NAME);
alter table Env add constraint ENV_PRODUCT_KEY unique (PRODUCT_ID, ENV_NAME);
alter table APP add constraint APP_TO_ENV_FK foreign key (ENV_ID) references Env;
alter table Env add constraint ENV_TO_PRODUCT_FK foreign key (PRODUCT_ID) references Product;
