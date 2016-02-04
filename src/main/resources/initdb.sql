CREATE TABLE appuser
(
  id bigint NOT NULL,
  email text NOT NULL UNIQUE,
  personalmission text,
  language int NOT NULL,
  salt char(32) NOT NULL,
  password char(128) NOT NULL,
  CONSTRAINT pk_id PRIMARY KEY (id)
);

CREATE TABLE passwordreset
(
	id bigint NOT NULL,
	tokenhash char(128) NOT NULL,
	expirationdate timestamp without time zone NOT NULL,
	used boolean NOT NULL,
	appuser_id bigint,
	CONSTRAINT pk_passwordreset PRIMARY KEY (id),
	CONSTRAINT fk_user FOREIGN KEY (appuser_id)
      REFERENCES appuser (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE role
(
  id bigint NOT NULL,
  name text NOT NULL,
  rolegoal text,
  appuser_id bigint,
  CONSTRAINT pk_role PRIMARY KEY (id),
  CONSTRAINT fk_user FOREIGN KEY (appuser_id)
      REFERENCES appuser (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);
      
CREATE TABLE task
(
  id bigint NOT NULL,
  name text NOT NULL,
  date date,
  time time without time zone,
  firstdate date,
  finished boolean NOT NULL,
  note text,
  important boolean NOT NULL,
  role_id bigint,
  CONSTRAINT pk_task PRIMARY KEY (id),
  CONSTRAINT fk_role FOREIGN KEY (role_id)
      REFERENCES role (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE passwordreset_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807;

CREATE SEQUENCE appuser_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807;
  
CREATE SEQUENCE role_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807;
  
CREATE SEQUENCE task_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807;

CREATE INDEX fki_role ON task (role_id);
CREATE INDEX fki_rappuser ON role (appuser_id);
CREATE INDEX fki_pappuser ON passwordreset (appuser_id);