create table APP (APP_ID varchar(255) not null, DEPLOYED boolean not null, DEPLOYED_DATE varchar(255), TESTED boolean, UPDATED_DATE varchar(255) not null, VERSION varchar(255) not null, ENV_ID varchar(255) not null, primary key (APP_ID));
create table Env (ENV_ID varchar(255) not null, DISPLAY_NAME varchar(255) not null, LIVE boolean, NEXT_ENV_ID varchar(255), TESTED boolean, UPDATED_DATE varchar(255) not null, primary key (ENV_ID));
alter table APP add constraint ENV_APP_KEY unique (ENV_ID, APP_ID);
alter table APP add constraint APP_TO_ENV_FK foreign key (ENV_ID) references Env;
