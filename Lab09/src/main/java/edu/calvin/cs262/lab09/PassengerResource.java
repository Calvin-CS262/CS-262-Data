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
 *    https://caluber-221319.appspot.com/caluber/v1/passenger
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
public class PassengerResource {

    /**
     * GET
     * This method gets the full list of passenger from the Passenger table.
     *
     * @return JSON-formatted list of passenger records (based on a root JSON tag of "items")
     * @throws SQLException
     */
    @ApiMethod(path="passengers", httpMethod=GET)
    public List<Passenger> getPassengers() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<Passenger> result = new ArrayList<Passenger>();
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectPassengers(statement);
            while (resultSet.next()) {
                Passenger p = new Passenger(
                        Integer.parseInt(resultSet.getString(1)),
                        Integer.parseInt(resultSet.getString(2))
                );
                result.add(p);
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
     * This method gets the passenger from the Passenger table with the given ID.
     *
     * @param id the ID of the requested passenger
     * @return if the passenger exists, a JSON-formatted passenger record, otherwise an invalid/empty JSON entity
     * @throws SQLException
     */
    @ApiMethod(path="passenger/{rideId}/{userId}", httpMethod=GET)
    public Passenger getPassenger(@Named("userId") int uId, @Named("rideId") int rId) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Passenger result = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectPassenger(uId, rId, statement);
            if (resultSet.next()) {
                result = new Passenger(
                        Integer.parseInt(resultSet.getString(1)),
                        Integer.parseInt(resultSet.getString(2))
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
     * PUT (not necessary?)
     * This method creates/updates an instance of Person with a given ID.
     * If the user doesn't exist, create a new user using the given field values.
     * If the user already exists, update the fields using the new user field values.
     * We do this because PUT is idempotent, meaning that running the same PUT several
     * times is the same as running it exactly once.
     * Any user ID value set in the passed user data is ignored.
     *
     * @param id     the ID for the user, assumed to be unique
     * @param user a JSON representation of the user; The id parameter overrides any id specified here.
     * @return new/updated user entity
     * @throws SQLException
     *
    @ApiMethod(path="user/{id}", httpMethod=PUT)
    public User putUser(User user, @Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            user.setId(id);
            resultSet = selectUser(id, statement);
            if (resultSet.next()) {
                updateUser(user, statement);
            } else {
                insertUser(user, statement);
            }
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return user;
    }
    */

    /**
     * POST
     * This method creates an instance of Person with a new, unique ID
     * number. We do this because POST is not idempotent, meaning that running
     * the same POST several times creates multiple objects with unique IDs but
     * otherwise having the same field values.
     *
     * The method creates a new, unique ID by querying the passenger table for the
     * largest ID and adding 1 to that. Using a DB sequence would be a better solution.
     * This method creates an instance of Person with a new, unique ID.
     *
     * @param passenger a JSON representation of the passenger to be created
     * @return new passenger entity with a system-generated ID
     * @throws SQLException
     */
    @ApiMethod(path="passenger", httpMethod=POST)
    public Passenger postPassenger(Passenger passenger) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        // ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            // resultSet = statement.executeQuery("SELECT MAX(ID) FROM Passenger");
            // if (resultSet.next()) {
            //     passenger.setId(resultSet.getInt(1) + 1);
            // } else {
            //     throw new RuntimeException("failed to find unique ID...");
            // }
            insertPassenger(passenger, statement);
        } catch (SQLException e) {
            throw (e);
        } finally {
            // if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return passenger;
    }

    /**
     * DELETE
     * This method deletes the instance of Person with a given ID, if it exists.
     * If the passenger with the given ID doesn't exist, SQL won't delete anything.
     * This makes DELETE idempotent.
     *
     * @param id     the ID for the passenger, assumed to be unique
     * @return the deleted passenger, if any
     * @throws SQLException
     */
    @ApiMethod(path="passenger/{rideId}/{userId}", httpMethod=DELETE)
    public void deletePassenger(@Named("userId") int uId, @Named("rideId") int rId) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            deletePassenger(uId, rId, statement);
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
    }

    /** SQL Utility Functions *********************************************/

    /*
     * This function gets the user with the given id using the given JDBC statement.
     */
    private ResultSet selectPassenger(int uId, int rId, Statement statement) throws SQLException {
        return statement.executeQuery(
                String.format("SELECT * FROM Passenger WHERE userId=%d, rideId=%d", uId, rId)
        );
    }

    /*
     * This function gets the user with the given id using the given JDBC statement.
     */
    private ResultSet selectPassengers(Statement statement) throws SQLException {
        return statement.executeQuery(
                "SELECT * FROM Passenger"
        );
    }

    /*
     * This function modifies the given user using the given JDBC statement. (PUT not needed)
     *
    private void updateUser(User user, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("UPDATE User SET password='%s', lastName=%s, firstName=%s, email=%s WHERE id=%d",
                        user.getPassword(),
                        getValueStringOrNull(user.getLastName()),
                        getValueStringOrNull(user.getFirstName()),
                        user.getEmail(),
                        user.getId()
                )
        );
    }
    */

    /*
     * This function inserts the given passenger using the given JDBC statement.
     */
    private void insertPassenger(Passenger passenger, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("INSERT INTO Passenger VALUES (%d, %d)",
                        passenger.getUser(),
                        passenger.getRide()
                )
        );
    }

    /*
     * This function gets the passenger with the given id using the given JDBC statement.
     */
    private void deletePassenger(int uId, int rId, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("DELETE FROM Passenger WHERE userId=%d, rideId=%d", uId, rId)
        );
    }

    /*
     * This function returns a value literal suitable for an SQL INSERT/UPDATE command.
     * If the value is NULL, it returns an unquoted NULL, otherwise it returns the quoted value.
     */
    private String getValueStringOrNull(String value) {
        if (value == null) {
            return "NULL";
        } else {
            return "'" + value + "'";
        }
    }

}