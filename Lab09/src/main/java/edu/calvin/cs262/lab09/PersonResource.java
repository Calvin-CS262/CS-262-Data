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
 *    https://caluber-221319.appspot.com/caluber/v2/persons
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
public class PersonResource {

    /**
     * GET
     * This method gets the full list of persons from the Person table.
     *
     * @return JSON-formatted list of person records (based on a root JSON tag of "items")
     * @throws SQLException
     */
    @ApiMethod(path="persons", httpMethod=GET)
    public List<Person> getPersons() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<Person> result = new ArrayList<Person>();
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectPersons(statement);
            while (resultSet.next()) {
                Person p = new Person(
                        Integer.parseInt(resultSet.getString(1)),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5)
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
     * This method gets the person from the Person table with the given ID.
     *
     * @param id the ID of the requested person
     * @return if the person exists, a JSON-formatted person record, otherwise an invalid/empty JSON entity
     * @throws SQLException
     */
    @ApiMethod(path="person/{id}", httpMethod=GET)
    public Person getPerson(@Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Person result = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = selectPerson(id, statement);
            if (resultSet.next()) {
                result = new Person(
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
     * If the person doesn't exist, create a new person using the given field values.
     * If the person already exists, update the fields using the new person field values.
     * We do this because PUT is idempotent, meaning that running the same PUT several
     * times is the same as running it exactly once.
     * Any person ID value set in the passed person data is ignored.
     *
     * @param id     the ID for the person, assumed to be unique
     * @param person a JSON representation of the person; The id parameter overrides any id specified here.
     * @return new/updated person entity
     * @throws SQLException
     */
    @ApiMethod(path="person/{id}", httpMethod=PUT)
    public Person putPerson(Person person, @Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            person.setPersonId(id);
            resultSet = selectPerson(id, statement);
            if (resultSet.next()) {
                updatePerson(person, statement);
            } else {
                insertPerson(person, statement);
            }
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return person;
    }

    /**
     * POST
     * This method creates an instance of Person with a new, unique ID
     * number. We do this because POST is not idempotent, meaning that running
     * the same POST several times creates multiple objects with unique IDs but
     * otherwise having the same field values.
     *
     * The method creates a new, unique ID by querying the person table for the
     * largest ID and adding 1 to that. Using a DB sequence would be a better solution.
     * This method creates an instance of Person with a new, unique ID.
     *
     * @param person a JSON representation of the person to be created
     * @return new person entity with a system-generated ID
     * @throws SQLException
     */
    @ApiMethod(path="person", httpMethod=POST)
    public Person postPerson(Person person) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT MAX(ID) FROM Person");
            if (resultSet.next()) {
                person.setPersonId(resultSet.getInt(1) + 1);
            } else {
                throw new RuntimeException("failed to find unique ID...");
            }
            insertPerson(person, statement);
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (resultSet != null) { resultSet.close(); }
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
        return person;
    }

    /**
     * DELETE
     * This method deletes the instance of Person with a given ID, if it exists.
     * If the person with the given ID doesn't exist, SQL won't delete anything.
     * This makes DELETE idempotent.
     *
     * @param id     the ID for the person, assumed to be unique
     * @return the deleted person, if any
     * @throws SQLException
     */
    @ApiMethod(path="person/{id}", httpMethod=DELETE)
    public void deletePerson(@Named("id") int id) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(System.getProperty("cloudsql"));
            statement = connection.createStatement();
            deletePerson(id, statement);
        } catch (SQLException e) {
            throw (e);
        } finally {
            if (statement != null) { statement.close(); }
            if (connection != null) { connection.close(); }
        }
    }

    /** SQL Utility Functions *********************************************/

    /*
     * This function gets the person with the given id using the given JDBC statement.
     */
    private ResultSet selectPerson(int id, Statement statement) throws SQLException {
        return statement.executeQuery(
                String.format("SELECT * FROM Person WHERE id=%d", id)
        );
    }

    /*
     * This function gets the person with the given id using the given JDBC statement.
     */
    private ResultSet selectPersons(Statement statement) throws SQLException {
        return statement.executeQuery(
                "SELECT * FROM Person"
        );
    }

    /*
     * This function modifies the given person using the given JDBC statement.
     */
    private void updatePerson(Person person, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("UPDATE Person SET password=%s, lastName=%s, firstName=%s, email=%s WHERE id=%d",
                        person.getPassword(),
                        getValueStringOrNull(person.getLastName()),
                        getValueStringOrNull(person.getFirstName()),
                        person.getEmail(),
                        person.getPersonId()
                )
        );
    }

    /*
     * This function inserts the given person using the given JDBC statement.
     */
    private void insertPerson(Person person, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("INSERT INTO Person VALUES (%d, %s, %s, %s, %s)",
                        person.getPersonId(),
                        person.getEmail(),
                        person.getPassword(),
                        getValueStringOrNull(person.getLastName()),
                        getValueStringOrNull(person.getFirstName())
                )
        );
    }

    /*
     * This function gets the person with the given id using the given JDBC statement.
     */
    private void deletePerson(int id, Statement statement) throws SQLException {
        statement.executeUpdate(
                String.format("DELETE FROM Person WHERE id=%d", id)
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