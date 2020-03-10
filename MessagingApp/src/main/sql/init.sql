\set QUIET true
SET client_min_messages TO WARNING; -- Less talk please.
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
\set QUIET false


CREATE TABLE passwords (
                           userName TEXT PRIMARY KEY,
                           hash TEXT NOT NULL --Borde nog ändras till någon annan typ
);

CREATE VIEW passwordView AS (
                            SELECT * FROM passwords
                                );


CREATE FUNCTION newUser()
    RETURNS trigger AS $$
BEGIN

    IF(EXISTS(
            SELECT userName
            FROM passwordView
            WHERE userName = New.userName)
        ) THEN
        RAISE EXCEPTION 'UserName already in use';

    ELSE
    INSERT INTO passwords VALUES (New.userName, New.hash);
    RAISE NOTICE 'User: % was added.', NEW.userName;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER newUserTrigger
    INSTEAD OF INSERT ON passwordView
    FOR EACH ROW EXECUTE PROCEDURE newUser();
