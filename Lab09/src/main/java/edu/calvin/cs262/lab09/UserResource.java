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
 *    https://caluber-221319.appspot.com/caluber/v2/users
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
public class UserResource {

    /**
     * GET
     * This method gets the full list of users from the User table.
     *
     * @return JSON-formatted list of user records (based on a root JSON tag of "items")
     * @throws SQLException
     */
    @ApiMethod(path="users", httpMethod=GET)
    public List<User> getUsers() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<User> result = new ArrayList<User>();
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectUsers(statement);
            while (resultSet.next()) {
                User u = new User(
                        Integer.parseInt(resultSet.getString(1)),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5)
                );
                result.add(u);
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
     * This method gets the user from the User table with the given ID.
     *
     * @param id the ID of the requested user
     * @return if the user exists, a JSON-formatted user record, otherwise an invalid/empty JSON entity
     * @throws SQLException
     */
    @ApiMethod(path="user/{id}", httpMethod=GET)
    public User getUser(@Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        User result = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectUser(id, statement);
            if (resultSet.next()) {
                result = new User(
                        Integer.parseInt(resultSet.getString(1)),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5)
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
     */
    @ApiMethod(path="user/{id}", httpMethod=PUT)
    public User putUser(User user, @Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            user.setUserId(id);
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

    /**
     * POST
     * This method creates an instance of Person with a new, unique ID
     * number. We do this because POST is not idempotent, meaning that running
     * the same POST several times creates multiple objects with unique IDs but
     * otherwise having the same field values.
     *
     * The method creates a new, unique ID by querying the user table for the
     * largest ID and adding 1 to that. Using a DB sequence would be a better solution.
     * This method creates an instance of Person with a new, unique ID.
     *
     * @param user a JSON representation of the user to be created
     * @return new user entity with a system-generated ID
     * @throws SQLException
     */
    @ApiMethod(path="user", httpMethod=POST)
    public User postUser(User user) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT MAX(ID) FROM User");
            if (resultSet.next()) {
                user.setUserId(resultSet.getInt(1) + 1);
            } else {
                throw new RuntimeException("failed to find unique ID...");
            }
            insertUser(user, statement);
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return user;
    }

    /**
     * DELETE
     * This method deletes the instance of Person with a given ID, if it exists.
     * If the user with the given ID doesn't exist, SQL won't delete anything.
     * This makes DELETE idempotent.
     *
     * @param id     the ID for the user, assumed to be unique
     * @return the deleted user, if any
     * @throws SQLException
     */
    @ApiMethod(path="user/{id}", httpMethod=DELETE)
    public void deleteUser(@Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            deleteUser(id, statement);
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
    private ResultSet selectUser(int id, Statement statement) throws SQLException {
        return statement.executeQuery(
                String.format("SELECT * FROM User WHERE id=%d", id)
        );
    }

    /*
     * This function gets the user with the given id using the given JDBC statement.
     */
    private ResultSet selectUsers(Statement statement) throws SQLException {
        return statement.executeQuery(
                "SELECT * FROM User"
        );
    }

    /*
     * This function modifies the given user using the given JDBC statement.
     */
    private void updateUser(User user, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("UPDATE User SET password=%s, lastName=%s, firstName=%s, email=%s WHERE id=%d",
                        user.getPassword(),
                        getValueStringOrNull(user.getLastName()),
                        getValueStringOrNull(user.getFirstName()),
                        user.getEmail(),
                        user.getUserId()
                )
        );
    }

    /*
     * This function inserts the given user using the given JDBC statement.
     */
    private void insertUser(User user, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("INSERT INTO User VALUES (%d, %s, %s, %s, %s)",
                        user.getUserId(),
                        user.getPassword(),
                        getValueStringOrNull(user.getLastName()),
                        getValueStringOrNull(user.getFirstName()),
                        user.getEmail()
                )
        );
    }

    /*
     * This function gets the user with the given id using the given JDBC statement.
     */
    private void deleteUser(int id, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("DELETE FROM User WHERE id=%d", id)
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