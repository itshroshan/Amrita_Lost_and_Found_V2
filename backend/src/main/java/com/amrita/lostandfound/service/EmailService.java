package com.amrita.lostandfound.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // This perfectly replaces your Python smtplib function
    @Async
    public void sendOtp(String toEmail, String otp) {
        System.out.println("OTP for " + toEmail + " is " + otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("OTP Verification - Lost & Found System");
        message.setText("Your OTP for login is: " + otp);

        try {
            mailSender.send(message);
            System.out.println("OTP Email successfully sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("FAILED to send OTP email to " + toEmail);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- NEW: DEDICATED MATCH NOTIFICATION METHOD ---
    @Async
    public void sendMatchNotification(String toEmail, String matchMessage) {
        System.out.println("Sending Match Notification to " + toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("Item Match Found! - Amrita Lost & Found"); // Clean subject line!
        message.setText(matchMessage); // Uses exactly the message we generate

        try {
            mailSender.send(message);
            System.out.println("Match Notification successfully sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("FAILED to send Match Notification to " + toEmail);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
        }
    }

}