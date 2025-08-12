package aut.ap;

import aut.ap.model.Email;
import aut.ap.model.EmailService;
import aut.ap.model.User;
import aut.ap.model.UserService;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static User user;
    private static boolean loggedIn = false;
    private static boolean viewing = false;

    private static SessionFactory sessionFactory;

    public static void main(String[] args) {
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();

        try {
            homePage();
        } finally {
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }

    public static void homePage() {
        System.out.println();

        while (true) {

            if (!loggedIn) {
                System.out.println("[L]ogin, [S]ign up, [E]xit: ");
            } else if (!viewing) {
                System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [E]xit: ");
            } else {
                System.out.println("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode, [B]ack, [E]xit: ");
            }

            System.out.print("> ");
            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("E")) {
                System.out.println("Exiting...");
                break;
            }

            if (!loggedIn) {
                handleFirstCommand(command);
            } else if (viewing) {
                viewProcessCommand(command);
            } else {
                handleLoginCommand(command);
            }

            System.out.println();
        }

        System.out.println();
    }


    private static void handleFirstCommand(String inputCommand) {
        try {
            String cmd = inputCommand.toUpperCase();
            if (cmd.equals("L")) {
                login();
            } else if (cmd.equals("S")) {
                signUp();
            } else {
                System.out.println("Unknown command: " + inputCommand);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private static void handleLoginCommand(String inputCommand) {
        try {
            String cmd = inputCommand.toUpperCase();
            if (cmd.equals("S")) {
                send();
            } else if (cmd.equals("V")) {
                viewing = true;
            } else if (cmd.equals("R")) {
                reply();
            } else if (cmd.equals("F")) {
                forward();
            } else {
                System.out.println("Unknown command: " + inputCommand);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private static void viewProcessCommand(String command) {
        try {
            switch (command.toUpperCase()) {
                case "A" -> allEmails();
                case "U" -> unreadEmails();
                case "S" -> sentEmails();
                case "C" -> readByCode();
                case "B" -> viewing = false;
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void login() {
        System.out.println("Enter your Email: ");
        String email = scanner.nextLine();

        System.out.println("Enter your Password: ");
        String password = scanner.nextLine();

        user = UserService.login(email, password);

        loggedIn = true;

        System.out.println("Welcome back, " + user.getFullName() + "!");
        System.out.println();

        unreadEmails();
    }

    private static void signUp() {
        System.out.println("Enter your Name: ");
        String name = scanner.nextLine();

        System.out.println("Enter your Email: ");
        String email = scanner.nextLine();

        System.out.println("Enter your Password: ");
        String password = scanner.nextLine();

        UserService.signUp(name, email, password);

        System.out.println("Your new account is created");
        System.out.println("Go ahead and login!");
    }

    private static void send() {
        System.out.print("Enter Recipient(s): ");
        String recipients = scanner.nextLine();

        System.out.print("Enter Subject: ");
        String subject = scanner.nextLine();

        System.out.println("Enter Body (leave empty line to finish): ");
        StringBuilder bodyBuilder = new StringBuilder();
        while (true) {
            String inputLine = scanner.nextLine();
            if (inputLine.isEmpty()) break;
            bodyBuilder.append(inputLine).append(System.lineSeparator());
        }
        String body = bodyBuilder.toString().trim();

        Email sentEmail = EmailService.send(user, recipients, subject, body);

        System.out.println("Email sent successfully.");
        System.out.println("Email Code: " + sentEmail.getUniqueCode());
    }


    private static void forward() {
        System.out.println("Code:");
        String code = scanner.nextLine();

        System.out.println("Enter Recipient(s): ");
        String recipients = scanner.nextLine();

        EmailService.forward(user, code, recipients);
    }

    private static void reply() {
        System.out.println("Code:");
        String code = scanner.nextLine();

        System.out.println("Enter Body: ");
        StringBuilder bodyBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).isEmpty()) {
            bodyBuilder.append(line).append("\n");
        }
        String body = bodyBuilder.toString().trim();

        EmailService.reply(user, code, body);
    }

    private static void sentEmails() {
        EmailService.sentEmails(user);
    }

    private static void allEmails() {
        EmailService.allEmails(user);
    }

    private static void unreadEmails() {
        EmailService.unreadEmails(user);
    }

    private static void readByCode() {
        System.out.println("Code:");
        String code = scanner.nextLine();

        EmailService.readByUniqeCode(user, code);
    }
}
