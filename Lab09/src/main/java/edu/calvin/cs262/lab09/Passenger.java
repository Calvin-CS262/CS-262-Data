package edu.calvin.cs262.lab09;

public class Passenger {
	private int id;
	private int personId;
	private int rideId;


	public Passenger(){
		// The JSON marshaller used by Endpoints requires this default constructor.
	}

	public Passenger(int id, int rideId, int personId) {
		this.id = id;
		this.rideId = rideId;
		this.personId = personId;
	}

	public int getID() {
		return this.id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getPerson() {
		return this.personId;
	}

	public int getRide() {
		return this.rideId;
	}
}