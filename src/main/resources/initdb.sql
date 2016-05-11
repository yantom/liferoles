CREATE TABLE appuser
(
  id bigint NOT NULL,
  email text NOT NULL UNIQUE,
  personalmission text DEFAULT "",
  firstDayOfWeek int NOT NULL DEFAULT 0,
  salt text NOT NULL,
  password text NOT NULL,
  CONSTRAINT pk_id PRIMARY KEY (id)
);

CREATE TABLE passwordreset
(
	id bigserial,
	tokenhash text NOT NULL,
	expirationdate timestamp without time zone NOT NULL,
	used boolean NOT NULL DEFAULT FALSE,
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
  appuser_id bigint,
  CONSTRAINT pk_role PRIMARY KEY (id),
  CONSTRAINT fk_user FOREIGN KEY (appuser_id)
      REFERENCES appuser (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE rolegoal
(
  id bigint NOT NULL,
  name text NOT NULL,
  finished boolean NOT NULL DEFAULT FALSE,
  role_id bigint,
  CONSTRAINT pk_rolegoals PRIMARY KEY (id),
  CONSTRAINT fk_role FOREIGN KEY (role_id)
      REFERENCES role (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);
      
CREATE TABLE task
(
  id bigint NOT NULL,
  name text NOT NULL,
  date date,
  time time without time zone,
  firstdate date,
  finished boolean NOT NULL DEFAULT FALSE,,
  note text,
  important boolean NOT NULL DEFAULT FALSE,
  appuser_id bigint,
  role_id bigint,
  CONSTRAINT pk_task PRIMARY KEY (id),
  CONSTRAINT fk_role FOREIGN KEY (role_id)
      REFERENCES role (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_user FOREIGN KEY (appuser_id)
      REFERENCES appuser (id) MATCH SIMPLE
);

CREATE TABLE tokens
(
	jit bigserial,
	appuser_id bigint,
	blacklist boolean NOT NULL DEFAULT FALSE,
	CONSTRAINT pk_jitblacklist PRIMARY KEY (jit),
	CONSTRAINT fk_user FOREIGN KEY (appuser_id)
      REFERENCES appuser (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE SEQUENCE rolegoal_id_seq
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

CREATE INDEX fki_grole ON rolegoal (role_id);
CREATE INDEX fki_role ON task (role_id);
CREATE INDEX fki_rappuser ON role (appuser_id);
CREATE INDEX fki_tappuser ON task (appuser_id);
CREATE INDEX fki_pappuser ON passwordreset (appuser_id);
CREATE INDEX fki_tokappuser on tokens (appuser_id);