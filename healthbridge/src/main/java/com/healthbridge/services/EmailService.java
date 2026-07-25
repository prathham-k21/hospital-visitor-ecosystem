package com.healthbridge.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendAdmissionEmail(String toEmail, String patientName, String roomNumber) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("no-reply@healthbridge.com"); // This is just a display label
        message.setTo(toEmail);
        message.setSubject("Welcome to XYZ Hospital - Admission Confirmed");

        String emailBody = "Dear " + patientName + ",\n\n"
                + "Thank you for choosing XYZ Hospital. You have been successfully admitted to Room: " + roomNumber + ".\n\n"
                + "Our medical staff will be with you shortly.\n We wish you a speedy recovery and are here to support you every step of the way. \nAll your reports will be shared to you on your email.\n\n"
                + "Get well soon,\n"
                + "The XYZ Hospital Team";

        message.setText(emailBody);

        mailSender.send(message);
    }
    public void sendDischargeEmail(String toEmail, String patientName) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("no-reply@healthbridge.com");
        message.setTo(toEmail);
        message.setSubject("Happy Discharge from XYZ Hospital! 🌟");

        String emailBody = "Dear " + patientName + ",\n\n"
                + "We are absolutely thrilled to see you well enough to go home! Your discharge process from XYZ Hospital is now complete.\n\n"
                + "Please make sure to rest up and follow any post-care instructions provided by your medical team. Wishing you continued health, happiness, and a wonderful recovery at home.\n\n"
                + "Warmest regards,\n"
                + "The XYZ Hospital Team";

        message.setText(emailBody);

        mailSender.send(message);
    }
}