package edu.calvin.cs262.lab09;
import java.lang.Object;

/** Ride Class
 * Sets up class for the Ride Table
 * A Ride includes a rideID, a driverId (a User), departure time, destination, passenger limit, date, time and status
 */
public class Ride {

    private int rideId; //Primary Key
    private int driverId; //Foreign Key
    private String departure;
    private String destination;
    private int passengerLimit;
    private Instant dateTime;


    // Constructor
    public Ride(int rideId, int driverId, int passengerLimit, String  departure,
                 String destination, String dateTime) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.departure = departure;
        this.destination = destination;
        this.passengerLimit = passengerLimit;
        this.dateTime = Instant.parse(dateTime);

    }

    public int getRideId() { return this.rideId; }

    public void setRideId(int rideId) { this.rideId = rideId; }

    public int getDriver() { return this.driverId; }

    public void setDriver(int driverId) {
        this.driverId = driverId;
    }

    public String getDeparture() {
        return this.departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() { return this.destination; }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPassengerLimit() {
        return this.passengerLimit;
    }

    public void setPassengerLimit(int passengerLimit) {
        this.passengerLimit = passengerLimit;
    }

    public String getDateTime() { return this.dateTime.toString(); }

    public void setDateTime(String dateTime) { this.date = Instant.parse(dateTime); }

    public boolean getStatus() {
        return dateTime.isAfter(Instant.now());  //true if ride has passed, false if ride is upcoming
    }
    
}
