-- This script builds a basic database for the CalUber app
-- 
-- @author Luke Steffen (lhs3)
-- Created on 10/23/2018

-- Drop tables if they exist
DROP TABLE IF EXISTS Passenger;
DROP TABLE IF EXISTS Ride;
DROP TABLE IF EXISTS Users;

-- Drop database if it exists
DROP DATABASE IF EXISTS CalUber;

CREATE DATABASE CalUber;

USE CalUber;

CREATE TABLE Users (
	id integer PRIMARY KEY,
	studentId varchar(5),
	password varchar(50),
	lName text,
	fName text
	);

CREATE TABLE Ride (
	id integer PRIMARY KEY,
	driver integer REFERENCES Users(id),
	passengerLimit integer,
	departure text,
	destination text,
	day date,
	time time
	status bit
	);

CREATE TABLE Passenger (
	rideId integer REFERENCES Ride(id),
	passengerId integer REFERENCES Users(id)
	);

GRANT SELECT ON Users TO PUBLIC;
GRANT SELECT ON Ride TO PUBLIC;
GRANT SELECT ON Passenger TO PUBLIC;

-- Sample data
INSERT INTO Users VALUES (1, 'lhs3', 'abc123', 'steffen', 'luke');
INSERT INTO Users VALUES (2, 'abc1', 'abc456', 'abc', 'def');


-- Sample queries
SELECT * FROM Ride
WHERE destination LIKE 'Chicago';

SELECT studentId + '@students.calvin.edu'
FROM Users;

SELECT * FROM Ride
WHERE status = 0;

SELECT fName + ' ' + lName
FROM Users;

SELECT id, driver, passengerId
FROM Ride INNER JOIN Passenger
ON Ride.id = Passenger.rideId;








