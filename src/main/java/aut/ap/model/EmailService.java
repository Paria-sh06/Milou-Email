package aut.ap.model;

import aut.ap.framework.SingletonSessionFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class EmailService {

    private EmailService() {}

    public static Email send(User sender, String to, String subject, String body) {
        String[] addressArray = to.trim().split("\\s*,\\s*");

        List<EmailRecipient> recipients = Arrays.stream(addressArray)
                .map(emailAddr -> {
                    User foundUser = UserService.getUserByEmail(emailAddr);
                    if (foundUser == null) {
                        System.out.println("No such recipient: " + emailAddr);
                        return null;
                    }
                    return new EmailRecipient(null, foundUser, false);
                })
                .filter(Objects::nonNull)
                .toList();

        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("No valid recipient found.");
        }

        Email email = new Email(sender, to, subject, body);

        SingletonSessionFactory.get().inTransaction(session -> {
            session.persist(email);
            recipients.forEach(recipient -> {
                recipient.setRelatedEmail(email);
                session.persist(recipient);
            });
        });

        return email;
    }


    public static void allEmails(User user) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "SELECT DISTINCT r.relatedEmail FROM EmailRecipient r WHERE r.recipient = :user";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("user", user);

            List<Email> emails = query.getResultList();

            if (emails.isEmpty()) {
                System.out.println("You don't have any emails");
                return;
            }

            System.out.println("All Emails:");
            System.out.println("Count: " + emails.size());

            printEmails(emails);
        });
    }


    public static void unreadEmails(User user) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "SELECT r.relatedEmail FROM EmailRecipient r WHERE r.recipient = :user AND r.readStatus = false";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("user", user);

            List<Email> emails = query.getResultList();

            if (emails.isEmpty()) {
                System.out.println("No emails found.");
            } else {
                System.out.println(emails.size() + " Unread Emails:");
                printEmails(emails);
            }
        });
    }

    public static void sentEmails(User user) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hqlEmails = "FROM Email e WHERE e.sender = :user";
            Query<Email> emailQuery = session.createQuery(hqlEmails, Email.class);
            emailQuery.setParameter("user", user);
            List<Email> emails = emailQuery.getResultList();

            if (emails.isEmpty()) {
                System.out.println("You haven't sent any emails");
                return;
            }

            System.out.println(emails.size() + " Sent Emails:");

            String hqlAllRecipients = "FROM EmailRecipient r WHERE r.relatedEmail IN :emails";
            Query<EmailRecipient> recQuery = session.createQuery(hqlAllRecipients, EmailRecipient.class);
            recQuery.setParameter("emails", emails);
            List<EmailRecipient> allRecipients = recQuery.getResultList();

            Map<Integer, List<EmailRecipient>> byEmailId = new HashMap<>();
            for (EmailRecipient er : allRecipients) {
                Integer eId = er.getRelatedEmail().getEmailId();
                byEmailId.computeIfAbsent(eId, k -> new ArrayList<>()).add(er);
            }

            for (Email email : emails) {
                List<EmailRecipient> recipients = byEmailId.getOrDefault(email.getEmailId(), Collections.emptyList());

                List<String> recipientEmails = new ArrayList<>();
                for (EmailRecipient r : recipients) {
                    recipientEmails.add(r.getRecipient().getEmailAddress());
                }

                String joinedRecipients = String.join(", ", recipientEmails);
                System.out.println("+ " + joinedRecipients + " - " + email.getTitle() + " (" + email.getUniqueCode() + ")");
            }
        });
    }

    public static void reply(User user, String code, String replyBody) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.uniqueCode = :code";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("code", code);

            Email originalEmail;
            try {
                originalEmail = query.getSingleResult();
            } catch (Exception e) {
                throw new IllegalArgumentException("No such email with code: " + code);
            }

            List<EmailRecipient> recipients = getRecipientsOfEmail(session, originalEmail);

            boolean isReplyAllowed = false;

            for (EmailRecipient recipient : recipients) {
                if (recipient.getRecipient().getEmailAddress().equals(user.getEmailAddress())) {
                    recipient.setReadStatus(true);
                    session.merge(recipient);
                    isReplyAllowed = true;
                    break;
                }
            }

            if (!isReplyAllowed && !originalEmail.getSender().getEmailAddress().equals(user.getEmailAddress())) {
                throw new IllegalArgumentException("You cannot reply to this email.");
            }

            Email replyEmail = new Email();
            replyEmail.setSender(user);
            replyEmail.setTitle("Re: " + originalEmail.getTitle());
            replyEmail.setMessageBody(replyBody);
            replyEmail.setCreationTime(LocalDate.now());

            replyEmail.setRecipients(originalEmail.getSender().getEmailAddress());

            session.persist(replyEmail);

            EmailRecipient emailRecipient = new EmailRecipient();
            emailRecipient.setRelatedEmail(replyEmail);
            emailRecipient.setRecipient(originalEmail.getSender());
            emailRecipient.setReadStatus(false);
            session.persist(emailRecipient);
        });
    }


    public static void forward(User user, String code, String forwardRecipients) {
        SingletonSessionFactory.get().inTransaction(session -> {
            Email email = session.createQuery(
                            "FROM Email e WHERE e.uniqueCode = :code", Email.class)
                    .setParameter("code", code)
                    .uniqueResultOptional()
                    .orElseThrow(() -> new IllegalArgumentException("No such email with code: " + code));

            List<EmailRecipient> recipients = getRecipientsOfEmail(session, email);
            boolean hasAccess = recipients.stream()
                    .filter(r -> r.getRecipient().getEmailAddress().equals(user.getEmailAddress()))
                    .peek(r -> {
                        r.setReadStatus(true);
                        session.merge(r);
                    })
                    .findFirst()
                    .isPresent();

            if (!hasAccess && !email.getSender().getEmailAddress().equals(user.getEmailAddress())) {
                throw new IllegalArgumentException("You cannot forward this email.");
            }

            String forwardSubject = "[fw] " + email.getTitle();
            Email forwardEmail = send(user, forwardRecipients, forwardSubject, email.getMessageBody());

            System.out.println("Successfully forwarded your email.");
            System.out.println("Code: " + forwardEmail.getUniqueCode());
        });
    }


    public static void readByUniqeCode(User user, String code) {
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.uniqueCode = :code";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("code", code);

            Email email;
            try {
                email = query.getSingleResult();
            } catch (Exception e) {
                throw new IllegalArgumentException("No such email with code: " + code);
            }

            List<EmailRecipient> recipients = getRecipientsOfEmail(session, email);

            boolean allowed = false;
            List<String> recipientEmails = new ArrayList<>();

            for (EmailRecipient recipient : recipients) {
                String emailAddress = recipient.getRecipient().getEmailAddress();
                recipientEmails.add(emailAddress);

                if (emailAddress.equals(user.getEmailAddress())) {
                    allowed = true;
                    recipient.setReadStatus(true);
                    session.merge(recipient);
                }
            }

            if (!allowed && !email.getSender().getEmailAddress().equals(user.getEmailAddress())) {
                throw new IllegalArgumentException("You cannot read this email.");
            }

            System.out.println("Code: " + code);
            System.out.println("Recipient(s): " + String.join(", ", recipientEmails));
            System.out.println("Subject: " + email.getTitle());
            System.out.println("Date: " + email.getCreationTime());
            System.out.println();
            System.out.println(email.getMessageBody());
        });
    }
    
    private static List<EmailRecipient> getRecipientsOfEmail(Session session, Email email) {
        String hqlRecipients = "FROM EmailRecipient r WHERE r.relatedEmail = :email";
        Query<EmailRecipient> recipientQuery = session.createQuery(hqlRecipients, EmailRecipient.class);
        recipientQuery.setParameter("email", email);
        return recipientQuery.getResultList();
    }

    public static void printEmails(List<Email> emails) {
        Iterator<Email> it = emails.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
