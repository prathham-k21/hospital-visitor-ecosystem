package com.securestay.controller;

import com.securestay.model.Visitor;
import com.securestay.service.VisitorService;
import com.securestay.dto.PatientResponseDTO;
import com.securestay.services.HealthBridgeClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitors")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor // Automatically creates a constructor to assign the final fields
public class VisitorController {

    // Make these private final to clear the "never assigned" warnings
    private final VisitorService service;
    private final HealthBridgeClientService healthBridgeClientService;

    @PostMapping("/check-in")
    public ResponseEntity<?> processCheckIn(@RequestBody Visitor visitor) {
        try {
            if (visitor.getPatientId() == null) {
                return ResponseEntity.badRequest().body("Error: Patient ID is required for check-in.");
            }

            PatientResponseDTO patient = healthBridgeClientService.verifyPatient(visitor.getPatientId());

            if (patient.getAdmitted() != null && !patient.getAdmitted()) {
                return ResponseEntity.badRequest()
                        .body("Access Denied: Patient " + patient.getFullName() + " is currently discharged.");
            }

            // --- THE FIX: Auto-fill the required fields for QR/Email logic ---
            visitor.setRoomNumber(patient.getRoomNumber());
            visitor.setHostName(patient.getFullName());
            visitor.setHostEmail(patient.getEmail());
            // -----------------------------------------------------------------

            Visitor savedVisitor = service.checkIn(visitor);

            return ResponseEntity.ok(savedVisitor);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ Verification Failed: " + e.getMessage());
        }
    }

    @PutMapping("/check-out/{id}")
    public Visitor processCheckOut(@PathVariable Long id) {
        return service.checkOut(id);
    }

    @GetMapping("/history")
    public List<Visitor> getFullHistory() {
        return service.getAllVisitors();
    }

    @GetMapping("/active")
    public List<Visitor> getActiveVisitors() {
        return service.getActiveVisitors();
    }
}