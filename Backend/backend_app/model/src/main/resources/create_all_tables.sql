CREATE TABLE IF NOT EXISTS "user" (
  user_id  SERIAL PRIMARY KEY,
  login    TEXT NOT NULL,
  password TEXT NOT NULL,
  token    TEXT NOT NULL
);