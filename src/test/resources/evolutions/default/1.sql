# --- !Ups

CREATE TABLE "token" (
  id BIGSERIAL PRIMARY KEY,
  created TIMESTAMP DEFAULT now(),
  prerequisite_ids varchar(1024),
  value VARCHAR(255) NOT NULL
);

CREATE TABLE "user" (
  activated boolean default false,
  email varchar(255) NOT NULL,
  first_name varchar(255) not null,
  id BIGSERIAL PRIMARY KEY,
  last_name varchar(255) not null,
  password varchar(255) NOT NULL,
  payment_mechanism varchar(255),
  payment_mechanisms varchar(1024),
  user_id varchar(255) NOT NULL
);

CREATE UNIQUE INDEX unique_user_id_user ON "user" (user_id);
CREATE INDEX index_email_user ON "user" (email);

# --- !Downs

DROP TABLE IF EXISTS "token" CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;