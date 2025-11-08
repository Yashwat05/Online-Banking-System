package com.bankapp.util;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Centralized mail utility for BankApp.
 * Handles OTPs, transaction alerts, low balance notifications, and password changes.
 */
public class EmailSender {

    private static final String FROM_EMAIL = "punjsingh5@gmail.com";
    private static final String PASSWORD = "pqss ubhk kflg yoqr"; // Gmail App Password

    // Create mail session
    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });
    }

    // Reusable sender method
    private static boolean sendMail(String toEmail, String subject, String body) {
        try {
            Message message = new MimeMessage(createSession());
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Email sent successfully to " + toEmail);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Email sending failed: " + e.getMessage());
            return false;
        }
    }

    // 1. OTP
    public static boolean sendOTP(String toEmail, String otp) {
        String subject = "Your BankApp Login OTP";
        String body = "Dear user,\n\nYour OTP for BankApp login is: " + otp +
                "\n\nThis OTP is valid for 2 minutes.\n\n- BankApp Security Team";
        return sendMail(toEmail, subject, body);
    }

    // 2. Deposit Confirmation
    public static void sendDepositAlert(String toEmail, String username, double amount, double balance) {
        String subject = "Deposit Confirmation";
        String body = "Dear " + username + ",\n\nYour deposit of ₹" + amount +
                " was successful.\nNew Balance: ₹" + balance +
                "\n\nThank you for banking with BankApp.";
        sendMail(toEmail, subject, body);
    }

    // 3. Withdrawal Alert
    public static void sendWithdrawalAlert(String toEmail, String username, double amount, double balance) {
        String subject = "Withdrawal Alert";
        String body = "Dear " + username + ",\n\nA withdrawal of ₹" + amount +
                " has been made from your account.\nRemaining Balance: ₹" + balance +
                "\n\nIf this wasn't you, contact BankApp support immediately.";
        sendMail(toEmail, subject, body);

        if (balance < 1000) {
            sendLowBalanceAlert(toEmail, username, balance);
        }
    }

    // 4. Low Balance Alert
    public static void sendLowBalanceAlert(String toEmail, String username, double balance) {
        String subject = "Low Balance Alert";
        String body = "Dear " + username + ",\n\nYour current balance is ₹" + balance +
                ", which is below the minimum required level.\n\nPlease deposit funds soon to avoid penalties.\n\n- BankApp";
        sendMail(toEmail, subject, body);
    }

    // 5. Fund Transfer Alerts (with balances)
    public static void sendDebitAlert(String toEmail, String username, String fromAcc, String toAcc, double amount, double remainingBalance) {
        String subject = "₹" + amount + " Debited from Your Account";
        String body = "Dear " + username + ",\n\nAn amount of ₹" + amount +
                " has been successfully transferred from your account " + fromAcc +
                " to account " + toAcc + ".\nRemaining Balance: ₹" + remainingBalance +
                "\n\nIf this was not you, please contact BankApp immediately.\n\n- BankApp Security Team";
        sendMail(toEmail, subject, body);
    }

    public static void sendCreditAlert(String toEmail, String username, String toAcc, String fromAcc, double amount, double newBalance) {
        String subject = "₹" + amount + " Credited to Your Account";
        String body = "Dear " + username + ",\n\nAn amount of ₹" + amount +
                " has been credited to your account " + toAcc +
                " from account " + fromAcc + ".\nNew Balance: ₹" + newBalance +
                "\n\nThank you for banking with BankApp.\n\n- BankApp Team";
        sendMail(toEmail, subject, body);
    }

    public static void sendTransferFailure(String toEmail, String username, String fromAcc, String toAcc, double amount) {
        String subject = "Fund Transfer Failed";
        String body = "Dear " + username + ",\n\nYour transfer of ₹" + amount +
                " from account " + fromAcc + " to " + toAcc +
                " could not be processed.\nPlease verify your balance and try again.";
        sendMail(toEmail, subject, body);
    }

    // 6. Password Change Notification
    public static void sendPasswordChangeAlert(String toEmail, String username) {
        String subject = "Password Changed Successfully";
        String body = "Dear " + username + ",\n\nYour BankApp password was changed successfully." +
                "\nIf this was not you, please reset your password immediately or contact BankApp support.";
        sendMail(toEmail, subject, body);
    }
}
