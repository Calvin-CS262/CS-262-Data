package edu.calvin.cs262.lab09;

/** Person Class
 * Sets up person class for Person Table
 * A Person is defined by a personID, emailID, password, lastName and firstName
 * Primitive getters and setters defined for each instance variable
 */
public class Person {
    private int personId;
    private String email;
    private String password; //encrypt
    private String lastName;
    private String firstName;

    public Person() {
    // The JSON marshaller used by Endpoints requires this default constructor.
    }

    public Person(int personId, String emailId, String password, String lastName, String firstName) {
        this.personId = personId;
        this.email = emailId + "@students.calvin.edu";
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
    }


    public int getPersonId() { return personId; }

    public void setPersonId(int personId) {this.personId = personId;}

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

}
