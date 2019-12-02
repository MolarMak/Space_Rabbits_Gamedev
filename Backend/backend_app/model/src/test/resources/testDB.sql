CREATE TABLE IF NOT EXISTS "user" (
  user_id  SERIAL PRIMARY KEY,
  login    TEXT NOT NULL,
  password TEXT NOT NULL,
  token    TEXT NOT NULL
);

DROP TABLE IF EXISTS "user";

CREATE TABLE IF NOT EXISTS "fact" (
  fact_id      int PRIMARY KEY,
  fact         TEXT NOT NULL,
  true_fact    TEXT NOT NULL,
  false_fact   TEXT NOT NULL,
  fact_version int  NOT NULL
);

DROP TABLE IF EXISTS "fact";