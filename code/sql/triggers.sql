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

DROP TRIGGER IF EXISTS pID_trigger ON Passenger; 

CREATE TRIGGER pID_trigger BEFORE INSERT 
ON Passenger FOR EACH ROW 
EXECUTE PROCEDUE next_id(); 


