# --- !Ups

CREATE TABLE "auth_token" (
  id UUID PRIMARY KEY,
  expiry TIMESTAMP DEFAULT now(),
  uid BIGINT NOT NULL
);

CREATE TABLE "user" (
  activated boolean default false,
  email varchar(255) NOT NULL,
  first_name varchar(255) not null,
  id BIGSERIAL PRIMARY KEY,
  last_name varchar(255) not null,
  password varchar(255) NOT NULL,
  user_id varchar(255) NOT NULL
);

CREATE UNIQUE INDEX unique_user_id_user ON "user" (user_id);
CREATE INDEX index_email_user ON "user" (email);

# --- !Downs

DROP TABLE IF EXISTS "user" CASCADE;
