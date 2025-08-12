package aut.ap.model;

import aut.ap.framework.SingletonSessionFactory;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class EmailService {

    private EmailService() {
    }

    public static List<Email> getSentEmails(User currentUser) {
        List<Email> sentMails = new ArrayList<>();
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.sender = :senderUser";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("senderUser", currentUser);
            sentMails.addAll(query.getResultList());
        });
        return sentMails;
    }

    public static void createAndSend(User sender, String recipients, String subject, String body) {
        SingletonSessionFactory.get().inTransaction(session -> {
            Email mail = new Email(sender, recipients, subject, body);
            session.persist(mail);
        });
    }

    public static Email findByUniqueCode(String code) {
        AtomicReference<Email> found = new AtomicReference<>();
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE e.uniqueCode = :codeParam";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("codeParam", code);
            List<Email> result = query.getResultList();
            if (!result.isEmpty()) {
                found.set(result.get(0));
            }
        });
        return found.get();
    }

    public static List<Email> loadInbox(User currentUser) {
        List<Email> inboxMails = new ArrayList<>();
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM Email e WHERE :receiverAddress MEMBER OF e.recipientList";
            Query<Email> query = session.createQuery(hql, Email.class);
            query.setParameter("receiverAddress", currentUser.getEmailAddress());
            inboxMails.addAll(query.getResultList());
        });
        return inboxMails;
    }
}
