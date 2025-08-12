package aut.ap.model;

import jakarta.persistence.*;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "Emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer emailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Transient
    private List<String> recipients;

    @Basic(optional = false)
    private String uniqueCode;

    @Basic(optional = false)
    private String title;

    @Basic(optional = false)
    private String messageBody;

    @Basic(optional = false)
    @Column(name = "creation_time")
    private LocalDate creationTime;

    public Email() {
    }

    public Email(User fromUser, String to, String title, String messageBody) {
        setUniqueCode();
        setSender(fromUser);
        setRecipients(to);
        setTitle(title);
        setMessageBody(messageBody);
        setCreationTime();
    }

    @Override
    public String toString() {
        return "[ " + sender.getEmailAddress() + " ] " + title + " {" + uniqueCode + "}";
    }

    public Integer getEmailId() {
        return emailId;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public User getSender() {
        return sender;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getTitle() {
        return title;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public LocalDate getCreationTime() {
        return creationTime;
    }

    public void setEmailId(Integer emailId) {
        this.emailId = emailId;
    }

    private void setUniqueCode() {
        final String pool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        random.ints(6, 0, pool.length())
                .mapToObj(pool::charAt)
                .forEach(builder::append);
        this.uniqueCode = builder.toString();
    }

    public void setSender(User fromUser) {
        this.sender = fromUser;
    }

    public void setRecipients(String addresses) {
        if (addresses == null || addresses.isBlank()) {
            throw new IllegalArgumentException("Recipient list must not be empty");
        }
        recipients = new ArrayList<>(Arrays.asList(addresses.split(",\\s*")));
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Email subject is required");
        }
        this.title = title.strip();
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    private void setCreationTime() {
        this.creationTime = LocalDate.now();
    }
}
