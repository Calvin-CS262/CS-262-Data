-- This script builds a basic database for the CalUber app
-- 
-- @author Luke Steffen (lhs3)
-- @author Nate Gamble (neg6)
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
	email varchar(30),
	password varchar(50),
	lastName text,
	firstName text
	);

CREATE TABLE Ride (
	rideId integer PRIMARY KEY,
	driverId integer REFERENCES Person(id),
	departure text,
	destination text,
	passengerLimit integer,
	departureDateTime text,
	status text	--false means upcoming ride, true means past ride
	);

CREATE TABLE Passenger (
	id integer PRIMARY KEY,
	rideId integer REFERENCES Ride(rideId),
	personId integer REFERENCES Person(id)
	);

GRANT SELECT ON Person TO PUBLIC;
GRANT SELECT ON Ride TO PUBLIC;
GRANT SELECT ON Passenger TO PUBLIC;

-- Sample data
INSERT INTO Person VALUES (1, 'lhs3', 'abc123', 'steffen', 'luke');
INSERT INTO Person VALUES (2, 'abc1', 'abc456', 'def', 'abc');
INSERT INTO Person VALUES (3, 'neg6', 'abc789', 'Gamble', 'Nate');
INSERT INTO Ride VALUES (1, 1, 'Calvin College', 'Denmark', 4, '2018-11-30T10:00:00Z', 'false');
INSERT INTO Ride VALUES (2, 2, 'Calvin College', 'Chicago', 4, '2018-10-30T15:00:00Z', 'true');
INSERT INTO Passenger VALUES (1, 2, 1);
INSERT INTO Passenger VALUES (2, 2, 3);


-- Sample queries
SELECT * FROM Ride
WHERE destination LIKE 'Chicago';

SELECT CONCAT (email, '@students.calvin.edu')
FROM Person;

SELECT * FROM Ride
WHERE status = 'false';	--false means upcoming ride, true means past ride

SELECT CONCAT (firstName, ' ', lastName)
FROM Person;

SELECT Ride.rideId, driverId, driverId
FROM Ride INNER JOIN Passenger
ON Ride.rideId = Passenger.rideId;


-- curl --request POST --header "Content-Type: application/json" \
-- --data '{"password":"abc012","lName":"Jonas","fName":"Micah","studentId":"ocd3"}' \
-- https://caluber-221319.appspot.com/caluber/v1/person

-- curl --request POST --header "Content-Type: application/json" \
-- --data '{"passengerId":"3","rideId":"1"}' https://caluber-221319.appspot.com/caluber/v1/passenger
--curl --request POST --header "Content-Type: application/json" --data '{}' https://caluber-221319.appspot.com/caluber/v1/person