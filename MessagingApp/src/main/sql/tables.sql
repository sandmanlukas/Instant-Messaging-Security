CREATE TABLE passwords (
  userName TEXT PRIMARY KEY,
  hash TEXT NOT NULL --Borde nog ändras till någon annan typ
);

CREATE VIEW passwordView AS (
  SELECT * FROM passwords
);
