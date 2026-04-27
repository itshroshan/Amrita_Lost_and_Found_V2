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
}