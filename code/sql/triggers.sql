/*CS166, Project 1
Created by Melissa Castillo
Creates unique IDs for passengers, starting at 251 */ 

CREATE LANGUAGE plpgsql; 
CREATE OR REPLACE FUNCTION next_id()
RETURNS "trigger" AS 
	'BEGIN 
	new.pID = nextval(''pID_seq''); 
	Return new; 
	END;'
LANGUAGE 'plpgsql' VOLATILE; 

CREATE OR REPLACE FUNCTION next_bookRef()
RETURNS "trigger" AS 
	'BEGIN 
	new.bookRef = nextval(''bookRef_seq''); 
	Return new; 
	END;'
LANGUAGE 'plpgsql' VOLATILE; 

DROP TRIGGER IF EXISTS pID_trigger ON Passenger; 
DROP TRIGGER IF EXISTS bookRef_trigger ON Booking; 

CREATE TRIGGER bookRef_trigger BEFORE INSERT 
ON Booking FOR EACH ROW 
EXECUTE PROCEDURE next_bookRef(); 

CREATE TRIGGER pID_trigger BEFORE INSERT 
ON Passenger FOR EACH ROW 
EXECUTE PROCEDUE next_id(); 


