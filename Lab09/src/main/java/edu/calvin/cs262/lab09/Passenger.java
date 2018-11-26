package edu.calvin.cs262.lab09;

public class Passenger {
	private int id;
	private int personId;
	private int rideId;


	public Passenger(){
		// The JSON marshaller used by Endpoints requires this default constructor.
	}

	public Passenger(int id, int trip, int person) {
		this.id = id;
		this.personId = person;
		this.rideId = trip;
	}

	public int getID() {
		return this.id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getUser() {
		return this.personId;
	}

	public int getRide() {
		return this.rideId;
	}
}