package aut.ap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Email_Recipients")
public class EmailRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_id", nullable = false)
    private Email relatedEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_read", nullable = false)
    private Boolean readStatus;

    public EmailRecipient() {
    }

    public EmailRecipient(Email relatedEmail, User recipient, Boolean isRead) {
        this.relatedEmail = relatedEmail;
        this.recipient = recipient;
        this.readStatus = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Email getRelatedEmail() {
        return relatedEmail;
    }

    public void setRelatedEmail(Email relatedEmail) {
        this.relatedEmail = relatedEmail;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public Boolean getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(Boolean isRead) {
        this.readStatus = isRead;
    }
}
