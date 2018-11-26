-- This script builds a basic database for the CalUber app
-- 
-- @author Luke Steffen (lhs3)
-- Created on 10/23/2018

-- Drop tables if they exist
DROP TABLE IF EXISTS Passenger;
DROP TABLE IF EXISTS Ride;
DROP TABLE IF EXISTS Person;

-- Drop database if it exists
-- DROP DATABASE IF EXISTS CalUber;

-- CREATE DATABASE CalUber;

-- USE CalUber;

CREATE TABLE Person (
	id integer PRIMARY KEY,
	studentId varchar(5),
	password varchar(50),
	lName text,
	fName text
	);

CREATE TABLE Ride (
	id integer PRIMARY KEY,
	driver integer REFERENCES Person(id),
	passengerLimit integer,
	departure text,
	destination text,
	departureDateTime text,
	status text	--false means upcoming ride, true means past ride
	);

CREATE TABLE Passenger (
	id integer PRIMARY KEY,
	rideId integer REFERENCES Ride(id),
	passengerId integer REFERENCES Person(id)
	);

GRANT SELECT ON Person TO PUBLIC;
GRANT SELECT ON Ride TO PUBLIC;
GRANT SELECT ON Passenger TO PUBLIC;

-- Sample data
INSERT INTO Person VALUES (1, 'lhs3', 'abc123', 'steffen', 'luke');
INSERT INTO Person VALUES (2, 'abc1', 'abc456', 'def', 'abc');
INSERT INTO Person VALUES (3, 'neg6', 'abc789', 'Gamble', 'Nate');
INSERT INTO Ride VALUES (1, 1, 4, 'Calvin College', 'Denmark', '2018-11-30T10:00:00Z', 'false');
INSERT INTO Ride VALUES (2, 2, 4, 'Calvin College', 'Chicago', '2018-10-30T15:00:00Z', 'true');
INSERT INTO Passenger VALUES (2, 1);
INSERT INTO Passenger VALUES (2, 3);


-- Sample queries
SELECT * FROM Ride
WHERE destination LIKE 'Chicago';

SELECT CONCAT (studentId, '@students.calvin.edu')
FROM Person;

SELECT * FROM Ride
WHERE status = 'false';	--false means upcoming ride, true means past ride

SELECT CONCAT (fName, ' ', lName)
FROM Person;

SELECT id, driver, passengerId
FROM Ride INNER JOIN Passenger
ON Ride.id = Passenger.rideId;

