package aut.ap.model;

import aut.ap.framework.SingletonSessionFactory;
import org.hibernate.query.Query;

import java.util.concurrent.atomic.AtomicReference;

public class UserService {

    private UserService() {
    }

    public static User login(String email, String password) {
        email = normalizeEmail(email);

        User foundUser = getUserByEmail(email);
        if (foundUser == null) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (!foundUser.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        return foundUser;
    }

    public static void signUp(String name, String email, String password) {
        SingletonSessionFactory.get().inTransaction(session -> {
            User newUser = new User(name, email, password);
            session.persist(newUser);
        });
    }

    public static User getUserByEmail(String email) {
        AtomicReference<User> ref = new AtomicReference<>();
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM User u WHERE u.emailAddress = :email";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("email", email);
            ref.set(query.uniqueResult());
        });
        return ref.get();
    }



    private static String normalizeEmail(String email) {
        email = email.trim();
        if (email.contains(" ")) {
            throw new IllegalArgumentException("Email cannot contain spaces");
        }
        if (!email.endsWith("@milou.com")) {
            email += "@milou.com";
        }
        return email;
    }
}
