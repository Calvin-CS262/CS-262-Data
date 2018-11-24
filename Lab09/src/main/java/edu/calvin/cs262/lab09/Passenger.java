package edu.calvin.cs262.lab09;

public class Passenger {
	private int userID;
	private int rideID;

	public Passenger(){

	}

	public Passenger(int trip, int person) {
		this.user = person;
		this.ride = trip;
	}

	public int getUser() {
		return this.user;
	}

	public int getRide() {
		return this.ride;
	}
}