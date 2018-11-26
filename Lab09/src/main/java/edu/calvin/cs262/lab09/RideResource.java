package edu.calvin.cs262.lab09;

import com.google.api.server.spi.config.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod.GET;
import static com.google.api.server.spi.config.ApiMethod.HttpMethod.PUT;
import static com.google.api.server.spi.config.ApiMethod.HttpMethod.POST;
import static com.google.api.server.spi.config.ApiMethod.HttpMethod.DELETE;


/**
 * This Java annotation specifies the general configuration of the Google Cloud endpoint API.
 * The name and version are used in the URL: https://PROJECT_ID.appspot.com/monopoly/v1/ENDPOINT.
 * The namespace specifies the Java package in which to find the API implementation.
 * The issuers specifies boilerplate security features that we won't address in this course.
 *
 * You should configure the name and namespace appropriately.
 */
@Api(
        name = "caluber",
        version = "v1",
        namespace =
        @ApiNamespace(
                ownerDomain = "lab09.cs262.calvin.edu",
                ownerName = "lab09.cs262.calvin.edu",
                packagePath = ""
        ),
        issuers = {
                @ApiIssuer(
                        name = "firebase",
                        issuer = "https://securetoken.google.com/YOUR-PROJECT-ID",
                        jwksUri =
                                "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system"
                                        + ".gserviceaccount.com"
                )
        }
)

/**
 * This class implements a RESTful service for the player table of the monopoly database.
 * Only the player table is supported, not the game or playergame tables.
 *
 * You can test the GET endpoints using a standard browser or cURL.
 *
 * % curl --request GET \
 *    https://caluber-221319.appspot.com/caluber/v2/rides
 *
 * % curl --request GET \
 *    https://calvincs262-monopoly.appspot.com/monopoly/v1/player/1
 *
 * You can test the full REST API using the following sequence of cURL commands (on Linux):
 * (Run get-players between each command to see the results.)
 *
 * // Add a new player (probably as unique generated ID #4).
 * % curl --request POST \
 *    --header "Content-Type: application/json" \
 *    --data '{"name":"test name...", "emailAddress":"test email..."}' \
 *    https://calvincs262-monopoly.appspot.com/monopoly/v1/player
 *
 * // Edit the new player (assuming ID #4).
 * % curl --request PUT \
 *    --header "Content-Type: application/json" \
 *    --data '{"name":"new test name...", "emailAddress":"new test email..."}' \
 *    https://calvincs262-monopoly.appspot.com/monopoly/v1/player/4
 *
 * // Delete the new player (assuming ID #4).
 * % curl --request DELETE \
 *    https://calvincs262-monopoly.appspot.com/monopoly/v1/player/4
 *
 */
public class RideResource {

    /**
     * GET
     * This method gets the full list of rides from the Ride table.
     *
     * @return JSON-formatted list of ride records (based on a root JSON tag of "items")
     * @throws SQLException
     */
    @ApiMethod(path="rides", httpMethod=GET)
    public List<Ride> getRides() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<Ride> result = new ArrayList<Ride>();
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectRides(statement);
            while (resultSet.next()) {
                Ride r = new Ride(
                        Integer.parseInt(resultSet.getString(1)),
                        Integer.parseInt(resultSet.getString(2)),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        Integer.parseInt(resultSet.getString(5)),
                        resultSet.getString(6),	//Need to make sure that datetime is stored in database as a string
                        resultSet.getString(7)
                );
                result.add(r);
            }
        } catch (SQLException e) {
            throw(e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return result;
    }

    /**
     * GET
     * This method gets the ride from the Ride table with the given ID.
     *
     * @param id the ID of the requested ride
     * @return if the ride exists, a JSON-formatted ride record, otherwise an invalid/empty JSON entity
     * @throws SQLException
     */
    @ApiMethod(path="ride/{id}", httpMethod=GET)
    public Ride getRide(@Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Ride result = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectRide(id, statement);
            if (resultSet.next()) {
                result = new Ride(
                        Integer.parseInt(resultSet.getString(1)),
                        Integer.parseInt(resultSet.getString(2)),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        Integer.parseInt(resultSet.getString(5)),
                        resultSet.getString(6),	//Need to make sure that datetime is stored in database as a string
                        resultSet.getString(7)
                );
            }
        } catch (SQLException e) {
            throw(e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return result;
    }

    /**
     * PUT
     * This method creates/updates an instance of Person with a given ID.
     * If the ride doesn't exist, create a new ride using the given field values.
     * If the ride already exists, update the fields using the new ride field values.
     * We do this because PUT is idempotent, meaning that running the same PUT several
     * times is the same as running it exactly once.
     * Any ride ID value set in the passed ride data is ignored.
     *
     * @param id     the ID for the ride, assumed to be unique
     * @param ride a JSON representation of the ride; The id parameter overrides any id specified here.
     * @return new/updated ride entity
     * @throws SQLException
     */
    @ApiMethod(path="ride/{id}", httpMethod=PUT)
    public Ride putRide(Ride ride, @Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            ride.setRideId(id);
            resultSet = selectRide(id, statement);
            if (resultSet.next()) {
                updateRide(ride, statement);
            } else {
                insertRide(ride, statement);
            }
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return ride;
    }

    /**
     * POST
     * This method creates an instance of Person with a new, unique ID
     * number. We do this because POST is not idempotent, meaning that running
     * the same POST several times creates multiple objects with unique IDs but
     * otherwise having the same field values.
     *
     * The method creates a new, unique ID by querying the Ride table for the
     * largest ID and adding 1 to that. Using a DB sequence would be a better solution.
     * This method creates an instance of Person with a new, unique ID.
     *
     * @param ride a JSON representation of the ride to be created
     * @return new ride entity with a system-generated ID
     * @throws SQLException
     */
    @ApiMethod(path="ride", httpMethod=POST)
    public Ride postRide(Ride ride) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT MAX(ID) FROM Ride");
            if (resultSet.next()) {
                ride.setRideId(resultSet.getInt(1) + 1);
            } else {
                throw new RuntimeException("failed to find unique ID...");
            }
            insertRide(ride, statement);
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return ride;
    }

    /**
     * DELETE
     * This method deletes the instance of Person with a given ID, if it exists.
     * If the ride with the given ID doesn't exist, SQL won't delete anything.
     * This makes DELETE idempotent.
     *
     * @param id     the ID for the ride, assumed to be unique
     * @return the deleted ride, if any
     * @throws SQLException
     */
    @ApiMethod(path="ride/{id}", httpMethod=DELETE)
    public void deleteRide(@Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            deleteRide(id, statement);
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
    }

    /** SQL Utility Functions *********************************************/

    /*
     * This function gets the ride with the given id using the given JDBC statement.
     */
    private ResultSet selectRide(int id, Statement statement) throws SQLException {
        return statement.executeQuery(
                String.format("SELECT * FROM Ride WHERE rideId=%d", id)
        );
    }

    /*
     * This function gets the ride with the given id using the given JDBC statement.
     */
    private ResultSet selectRides(Statement statement) throws SQLException {
        return statement.executeQuery(
                "SELECT * FROM Ride"
        );
    }

    /*
     * This function modifies the given ride using the given JDBC statement.
     */
    private void updateRide(Ride ride, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("UPDATE Ride SET driverId=%d, departure='%s', destination='%s', passengerLimit=%d, departureDateTime='%s', status = '%s' WHERE rideId=%d",
                        ride.getDriver(),
                        ride.getDeparture(),
                        ride.getDestination(),
                        ride.getPassengerLimit(),
                        ride.getDateTime(),
                        ride.getStatus(),
                        ride.getRideId()
                )
        );
    }

    /*
     * This function inserts the given ride using the given JDBC statement.
     */
    private void insertRide(Ride ride, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("INSERT INTO Ride VALUES (%d, %d, '%s', '%s', %d, '%s', '%s')",
                        ride.getRideId(),
                        ride.getDriver(),
                        ride.getDeparture(),
                        ride.getDestination(),
                        ride.getPassengerLimit(),
                        ride.getDateTime(),
                        ride.getStatus()
                )
        );
    }

    /*
     * This function gets the ride with the given id using the given JDBC statement.
     */
    private void deleteRide(int id, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("DELETE FROM Ride WHERE rideId=%d", id)
        );
    }

}