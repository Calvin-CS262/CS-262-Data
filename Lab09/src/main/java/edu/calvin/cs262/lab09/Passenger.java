package edu.calvin.cs262.lab09;

public class Passenger {
	private int id;
	private int userID;
	private int rideID;

	public Passenger(){

	}

	public Passenger(int id, int trip, int person) {
		this.id = id;
		this.userID = person;
		this.rideID = trip;
	}

	public int getID() {
		return this.id;
	}

	public int getUser() {
		return this.userID;
	}

	public int getRide() {
		return this.rideID;
	}
}