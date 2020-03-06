
CREATE FUNCTION newUser()
  RETURNS trigger AS $$
BEGIN

IF(EXISTS(
  SELECT userName
  FROM passwordView
  WHERE userName = New.userName)
) THEN
  RAISE EXCEPTION 'UserName already in use';
END IF;

INSERT INTO passwords VALUES (New.userName, New.hash);

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER newUserTrigger
  INSTEAD OF INSERT ON passwordView
  FOR EACH ROW EXECUTE PROCEDURE newUser();
