package aut.ap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "email_recipients")
public class EmailRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

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

    public EmailRecipient(Email relatedEmail, User receiverUser, Boolean readStatus) {
        this.relatedEmail = relatedEmail;
        this.recipient = receiverUser;
        this.readStatus = readStatus;
    }

    public int getRecipientId() {
        return Id;
    }

    public void setRecipientId(int Id) {
        this.Id = Id;
    }

    public Email getRelatedEmail() {
        return relatedEmail;
    }

    public void setRelatedEmail(Email relatedEmail) {
        this.relatedEmail = relatedEmail;
    }

    public void setReadStatus(Boolean readStatus) {
        this.readStatus = readStatus;
    }

    public Boolean getReadStatus() {
        return readStatus;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }


}
