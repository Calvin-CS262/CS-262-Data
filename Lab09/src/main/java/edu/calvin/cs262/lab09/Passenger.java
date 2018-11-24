package edu.calvin.cs262.lab09;

public class Passenger {
	private int userID;
	private int rideID;

	public Passenger(){

	}

	public Passenger(int trip, int person) {
		this.userID = person;
		this.rideID = trip;
	}

	public int getUser() {
		return this.userID;
	}

	public int getRide() {
		return this.rideID;
	}
}