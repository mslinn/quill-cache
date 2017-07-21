# --- !Ups

CREATE TABLE "token" (
  id BIGSERIAL PRIMARY KEY,
  created TIMESTAMP DEFAULT now(),
  related_id BIGINT,
  home_page VARCHAR(255),
  favorite_sites VARCHAR(1024),
  prerequisite_ids VARCHAR(1024),
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
  wrong_answer_map VARCHAR(2048),
  question_ids VARCHAR(1024),
  correct_answer_ids VARCHAR(1024),
  written_score INTEGER,
  user_id varchar(255) NOT NULL
);

CREATE UNIQUE INDEX unique_user_id_user ON "user" (user_id);
CREATE INDEX index_email_user ON "user" (email);

# --- !Downs

DROP INDEX IF EXISTS unique_user_id_user;
DROP INDEX IF EXISTS index_email_user;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS "token" CASCADE;
