package com.securestay.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitors")
@Data // Automatically creates Getters and Setters
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String hostName;
    private String hostEmail;
    private String purpose;

    // --- Added for HealthBridge Integration ---
    private String patientId;
    private String roomNumber;
    // ------------------------------------------

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    private String location; // We will fill this using the IP Geolocation API later
    private String status;   // "IN" or "OUT"
}