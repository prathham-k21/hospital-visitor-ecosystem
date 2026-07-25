package com.securestay.service;

import com.securestay.model.Visitor;
import com.securestay.repository.VisitorRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class VisitorService {

    @Autowired
    private VisitorRepository repository;

    @Autowired
    private JavaMailSender mailSender;

    public List<Visitor> getAllVisitors() {
        return repository.findAll();
    }

    public List<Visitor> getActiveVisitors() {
        return repository.findByStatus("IN");
    }

    public Visitor checkIn(Visitor visitor) {
        visitor.setCheckInTime(LocalDateTime.now());
        visitor.setStatus("IN");

        // Basic validation
        if (visitor.getEmail() == null || !visitor.getEmail().contains("@")) {
            throw new RuntimeException("Invalid Visitor Email");
        }

        Visitor savedVisitor = repository.save(visitor);

        // 1. Send Simple Email to Host
        sendSimpleEmail(
                visitor.getHostEmail(),
                "Visitor Arrived: " + savedVisitor.getName(),
                "Hello, your visitor " + savedVisitor.getName() + " has arrived."
        );

        // 2. Send HTML Email with QR Pass to Visitor
        sendHtmlEmailWithQR(savedVisitor);

        return savedVisitor;
    }

    public Visitor checkOut(Long id) {
        Visitor visitor = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visitor not found"));

        visitor.setCheckOutTime(LocalDateTime.now());
        visitor.setStatus("OUT");

        Visitor savedVisitor = repository.save(visitor);

        sendSimpleEmail(
                visitor.getEmail(),
                "Check-out Confirmed",
                "Hello " + visitor.getName() + ", you have checked out successfully."
        );

        return savedVisitor;
    }

    private void sendHtmlEmailWithQR(Visitor visitor) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart message (required for attachments/CIDs)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(visitor.getEmail());
            helper.setSubject("Your SecureStay Digital Pass");

            // 1. Generate the QR Code as a byte array
            byte[] qrCodeBytes = generateQRCodeBytes(visitor.getId().toString());

            // 2. Build HTML using 'cid:qrCodeImage' to reference the attachment
            String htmlContent = "<html><body style='font-family: sans-serif; text-align: center;'>" +
                    "<h2 style='color: #6366f1;'>Welcome to SecureStay</h2>" +
                    "<p>Hello <b>" + visitor.getName() + "</b>,</p>" +
                    "<p>Your check-in is confirmed. Please use the QR code below for checkout:</p>" +
                    "<div style='margin: 20px auto;'>" +
                    "<img src='cid:qrCodeImage' width='250' height='250' style='border: 2px solid #6366f1; border-radius: 10px;' />" +
                    "</div>" +
                    "<p>Scan this at the exit terminal. Enjoy your visit!</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);

            // 3. Attach the byte array as an inline resource with the ID 'qrCodeImage'
            helper.addInline("qrCodeImage", new org.springframework.core.io.ByteArrayResource(qrCodeBytes), "image/png");

            mailSender.send(message);
            System.out.println("QR Attachment Email sent successfully!");

        } catch (Exception e) {
            System.err.println("Error sending QR email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] generateQRCodeBytes(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    private String generateQRCodeBase64(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        // Explicitly flush the stream to ensure all bytes are captured
        outputStream.flush();
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();

        // Use the Basic encoder and ensure NO line breaks
        return Base64.getEncoder().encodeToString(imageBytes).trim();
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}