package aut.ap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Basic(optional = false)
    private String fullName;

    @Basic(optional = false)
    private String emailAddress;

    @Basic(optional = false)
    private String password;

    public User() {
    }

    public User(String fullName, String emailAddress, String passcode) {
        this.setFullName(fullName);
        this.setEmail(emailAddress);
        this.setPassword(passcode);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            throw new IllegalArgumentException("Name field must not be empty.");
        }
        this.fullName = fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmail(String emailAddress) {
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email field is required.");
        }
        String trimmedEmail = emailAddress.trim();
        if (trimmedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email field must not be empty.");
        }
        if (trimmedEmail.contains(" ")) {
            throw new IllegalArgumentException("Email cannot contain whitespace.");
        }
        if (!trimmedEmail.endsWith("@milou.com")) {
            trimmedEmail = trimmedEmail + "@milou.com";
        }
        this.emailAddress = trimmedEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passcode) {
        if (passcode == null || passcode.isBlank()) {
            throw new IllegalArgumentException("Password field cannot be blank.");
        }
        if (passcode.length() < 8) {
            throw new IllegalArgumentException("Password length must be 8 characters or more.");
        }
        this.password = passcode;
    }

    public Integer getUserId() {
        return userId;
    }
}
