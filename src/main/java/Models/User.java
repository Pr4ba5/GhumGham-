package Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String userType;
    private String nationality;

    public User() {
        this.userType = "user";
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public User(String firstName, String lastName, String email, String phone, String password, String nationality) {
        this();
        this.id = String.valueOf(System.currentTimeMillis());
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.nationality = nationality;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + email + ")";
    }
}
