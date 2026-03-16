package org.example.assignment2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail,String otp){

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("StudyHub Password Reset");
        message.setText("Your OTP code: " + otp);

        mailSender.send(message);
    }

    public void sendAccountEmail(String toEmail, String password) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Your StudyHub Account");

        message.setText(
                "Your account has been created successfully.\n\n" +
                        "Login Information:\n" +
                        "Email: " + toEmail + "\n" +
                        "Password: " + password + "\n\n" +
                        "Please login and change your password.\n\n" +
                        "StudyHub Team"
        );

        mailSender.send(message);
    }
}

