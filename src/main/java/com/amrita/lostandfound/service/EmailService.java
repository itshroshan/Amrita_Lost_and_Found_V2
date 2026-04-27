package com.amrita.lostandfound.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // This perfectly replaces your Python smtplib function
    public void sendOtp(String toEmail, String otp) {
        System.out.println("OTP for " + toEmail + " is " + otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OTP Verification - Lost & Found System");
        message.setText("Your OTP for login is: " + otp);

        mailSender.send(message);
    }

    // --- NEW: DEDICATED MATCH NOTIFICATION METHOD ---
    public void sendMatchNotification(String toEmail, String matchMessage) {
        System.out.println("Sending Match Notification to " + toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Item Match Found! - Amrita Lost & Found"); // Clean subject line!
        message.setText(matchMessage); // Uses exactly the message we generate

        mailSender.send(message);
    }

}